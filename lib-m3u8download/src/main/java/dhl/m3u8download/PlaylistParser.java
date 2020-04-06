package dhl.m3u8download;

import dhl.m3u8download.model.Key;
import dhl.m3u8download.model.MasterPlaylist;
import dhl.m3u8download.model.MediaPlaylist;
import dhl.m3u8download.model.MediaSegment;
import dhl.m3u8download.model.Playlist;
import dhl.m3u8download.model.VariantStream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Author: duanhl
 * Create: 2020-03-26 15:08
 * Description:https://developer.apple.com/streaming/examples/
 */
public class PlaylistParser {

  private static final String TAGS_COMMENTS_PREFIX = "#";

  /**
   * basic tag
   */
  private static final String BASIC_TAG = "#EXTM3U";

  /**
   * Master playlist tags
   */
  private static final String MASTER_TAG_X_MEDIA = "#EXT-X-MEDIA";
  private static final String MASTER_TAG_X_STREAM_INF = "#EXT-X-STREAM-INF";

  /**
   * Master playlist tags attributes
   */
  private static final String MASTER_ATTR_BANDWIDTH = "BANDWIDTH";
  private static final String MASTER_ATTR_CODECS = "CODECS";
  private static final String MASTER_ATTR_RESOLUTION = "RESOLUTION";
  private static final String MASTER_ATTR_FRAME_RATE = "FRAME-RATE";
  private static final String MASTER_ATTR_NAME = "NAME";

  /**
   * Media playlist tags
   */
  private static final String MEDIA_TAG_TARGETDURATION = "#EXT-X-TARGETDURATION";
  private static final String MEDIA_TAG_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE";
  private static final String MEDIA_TAG_TYPE = "#EXT-X-PLAYLIST-TYPE";
  private static final String MEDIA_TAG_ENDLIST = "#EXT-X-ENDLIST";
  private static final String MEDIA_TAG_EXTINF = "#EXTINF";
  private static final String MEDIA_TAG_BYTERANGE = "#EXT-X-BYTERANGE";
  private static final String MEDIA_TAG_DISCONTINUITY = "#EXT-X-DISCONTINUITY";
  private static final String MEDIA_TAG_KEY = "#EXT-X-KEY";

  /**
   * Key attributes
   */
  private static final String KEY_ATTR_METHOD = "METHOD";
  private static final String KEY_ATTR_URI = "URI";
  private static final String KEY_ATTR_IV = "IV";
  private static final String KEY_ATTR_KEYFORMAT = "KEYFORMAT";
  private static final String KEY_ATTR_KEYFORMATVERSIONS = "KEYFORMATVERSIONS";

  public static Playlist parsePlaylist(String path) throws Exception {
    BufferedReader reader = null;
    reader = new BufferedReader(new FileReader(path));

    MasterPlaylist masterPlaylist = null;
    VariantStream stream = null;

    MediaPlaylist mediaPlaylist = null;
    MediaSegment segment = null;
    Key key = null;

    String line = null;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith(TAGS_COMMENTS_PREFIX)) {//tags or comment line
        //Master Playlist tags
        if (line.startsWith(MASTER_TAG_X_STREAM_INF)) {
          if (masterPlaylist == null) {
            masterPlaylist = new MasterPlaylist();
          }
          stream = new VariantStream();

          Map<String, String> map = attrToMap(line);
          stream.setBandwidth(Integer.parseInt(map.get(MASTER_ATTR_BANDWIDTH)));
          stream.setCodecs(map.get(MASTER_ATTR_CODECS));
          if (map.containsKey(MASTER_ATTR_FRAME_RATE)) {
            stream.setFrameRate(Integer.parseInt(map.get(MASTER_ATTR_FRAME_RATE)));
          }
          stream.setResolution(map.get(MASTER_ATTR_RESOLUTION));
          stream.setName(map.get(MASTER_ATTR_NAME));
          continue;
        }

        //Media Playlist tags
        if (line.startsWith(MEDIA_TAG_TARGETDURATION)) {
          //#EXT-X-TARGETDURATION:3
          String[] s = line.split(":");
          int duration = Integer.parseInt(s[1]);
          if (mediaPlaylist == null) {
            mediaPlaylist = new MediaPlaylist();
          }
          mediaPlaylist.setTargetDuration(duration);
        } else if (line.startsWith(MEDIA_TAG_SEQUENCE)) {
          //#EXT-X-MEDIA-SEQUENCE:<number>
          String[] s = line.split(":");
          int seq = Integer.parseInt(s[1]);
          if (mediaPlaylist == null) {
            mediaPlaylist = new MediaPlaylist();
          }
          mediaPlaylist.setSequence(seq);
        } else if (line.startsWith(MEDIA_TAG_TYPE)) {
          //#EXT-X-PLAYLIST-TYPE:VOD|EVENT
          if (line.endsWith("VOD")) {
            if (mediaPlaylist == null) {
              mediaPlaylist = new MediaPlaylist();
            }
            mediaPlaylist.setVod(true);
          }
        } else if (line.startsWith(MEDIA_TAG_ENDLIST)) {
          if (mediaPlaylist == null) {
            mediaPlaylist = new MediaPlaylist();
          }
          mediaPlaylist.setVod(true);
          break;
        } else if (line.startsWith(MEDIA_TAG_EXTINF)) {
          //#EXTINF:<duration>,[<title>]
          if (segment == null) {
            segment = new MediaSegment();
          }
          String duration = line.substring(line.indexOf(":") + 1, line.indexOf(","));
          float dur = Float.parseFloat(duration);
          segment.setDuration(dur);
        } else if (line.startsWith(MEDIA_TAG_BYTERANGE)) {
          //#EXT-X-BYTERANGE:<n>[@<o>]
          if (segment == null) {
            segment = new MediaSegment();
          }

          String[] s = line.split(":");
          s = s[1].split("@");
          if (s.length > 0) {
            segment.setRangeLength(Integer.parseInt(s[0]));
          }
          if (s.length > 1) {
            segment.setRangeStart(Integer.parseInt(s[1]));
          } else {
            List<MediaSegment> segments = mediaPlaylist == null ? null : mediaPlaylist.getMediaSegments();
            MediaSegment last = segments == null || segments.isEmpty() ? null : segments.get(segments.size() - 1);
            if (last == null) {
              throw new M3u8DownloadException(M3u8DownloadException.ERRNO_PARSE_SUBRANGE_SEGMENT, "subrange error");
            }
            segment.setRangeStart(last.getRangeStart() + last.getRangeLength());
          }
        } else if (line.startsWith(MEDIA_TAG_DISCONTINUITY)) {
          if (segment == null) {
            segment = new MediaSegment();
          }
          segment.setDiscontinuity(true);
        } else if (line.startsWith(MEDIA_TAG_KEY)) {
          boolean supportEncrypt = false;
          if (!supportEncrypt) {
            throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ENCRYPT_STREAM, "No support encrypt stream");
          }
          //#EXT-X-KEY:<attribute-list>
          if (segment == null) {
            segment = new MediaSegment();
          }
          Map<String, String> attrs = attrToMap(line);
          key = new Key();
          key.setMethod(attrs.get(KEY_ATTR_METHOD));
          key.setUri(attrs.get(KEY_ATTR_URI));
          key.setIv(attrs.get(KEY_ATTR_IV));
          key.setKeyformat(attrs.get(KEY_ATTR_KEYFORMAT));
          key.setKeyformatVersions(attrs.get(KEY_ATTR_KEYFORMATVERSIONS));
        }
      } else if (line.trim().isEmpty()) {//blank line
        //ignore
      } else {//uri line
        if (stream != null) {
          stream.setUri(line);
          masterPlaylist.addVariantStream(stream);
          stream = null;
        } else if (segment != null) {
          segment.setUri(line);
          segment.setKey(key);
          if (mediaPlaylist == null) {
            mediaPlaylist = new MediaPlaylist();
          }
          mediaPlaylist.addMediaSegment(segment);
          segment = null;
        }
      }
    }

    if (masterPlaylist == null && mediaPlaylist == null) {
      throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "parse Playlist error");
    }
    return masterPlaylist == null ? mediaPlaylist : masterPlaylist;
  }

  private static Map<String, String> attrToMap(String tag) {
    //#EXT-X-STREAM-INF:BANDWIDTH=105040,CODECS="mp4a.40.5,avc1.42000b",RESOLUTION=192x112,NAME="144"
    Pattern pattern = Pattern.compile("[-A-Z0-9]+=((\\\".*?\\\")|(.*?))(?=,|$)");
    Matcher matcher = pattern.matcher(tag);
    Map<String, String> map = new HashMap<>();
    while (matcher.find()) {
      String pair = matcher.group();
      String[] kv = pair.split("=");
      map.put(kv[0], kv[1]);
    }
    return map;
  }

  public static boolean isMasterPlaylist(String content) {
    return content != null && content.contains(BASIC_TAG) && !content.contains(MEDIA_TAG_TARGETDURATION);
  }

  public static MasterPlaylist parseMasterPlaylist(String content) {
    if (content == null) {
      return null;
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(content));
      String line = null;
      MasterPlaylist masterPlaylist = new MasterPlaylist();
      VariantStream stream = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(TAGS_COMMENTS_PREFIX)) {//tags or comment line
          if (line.startsWith(MASTER_TAG_X_STREAM_INF)) {
            stream = new VariantStream();

            Map<String, String> map = attrToMap(line);
            stream.setBandwidth(Integer.parseInt(map.get(MASTER_ATTR_BANDWIDTH)));
            stream.setCodecs(map.get(MASTER_ATTR_CODECS));
            if (map.containsKey(MASTER_ATTR_FRAME_RATE)) {
              stream.setFrameRate(Integer.parseInt(map.get(MASTER_ATTR_FRAME_RATE)));
            }
            stream.setResolution(map.get(MASTER_ATTR_RESOLUTION));
          }
        } else if (line.trim().isEmpty()) {//blank line
          //ignore
        } else {//uri line
          if (stream != null) {
            stream.setUri(line);
            masterPlaylist.addVariantStream(stream);
            stream = null;
          }
        }
      }
      return masterPlaylist;
    } catch (Exception e) {
      return null;
    } finally {
      M3u8Util.close(reader);
    }
  }

  public static MediaPlaylist parseMediaPlaylist(String content) {
    if (content == null) {
      return null;
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(content));
      String line = null;
      MediaPlaylist mediaPlaylist = new MediaPlaylist();
      MediaSegment segment = null;
      Key key = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(TAGS_COMMENTS_PREFIX)) {//tags or comment line

          if (line.startsWith(MEDIA_TAG_TARGETDURATION)) {
            //#EXT-X-TARGETDURATION:3
            String[] s = line.split(":");
            int duration = Integer.parseInt(s[1]);
            mediaPlaylist.setTargetDuration(duration);
          } else if (line.startsWith(MEDIA_TAG_SEQUENCE)) {
            //#EXT-X-MEDIA-SEQUENCE:<number>
            String[] s = line.split(":");
            int seq = Integer.parseInt(s[1]);
            mediaPlaylist.setSequence(seq);
          } else if (line.startsWith(MEDIA_TAG_TYPE)) {
            //#EXT-X-PLAYLIST-TYPE:VOD|EVENT
            if (line.endsWith("VOD") || content.contains(MEDIA_TAG_ENDLIST)) {
              mediaPlaylist.setVod(true);
            }
          } else if (line.startsWith(MEDIA_TAG_ENDLIST)) {
            break;
          } else if (line.startsWith(MEDIA_TAG_EXTINF)) {
            //#EXTINF:<duration>,[<title>]
            if (segment == null) {
              segment = new MediaSegment();
            }
            String duration = line.substring(line.indexOf(":") + 1, line.indexOf(","));
            float dur = Float.parseFloat(duration);
            segment.setDuration(dur);
          } else if (line.startsWith(MEDIA_TAG_BYTERANGE)) {// TODO: 2020-03-27 dhl test
            //#EXT-X-BYTERANGE:<n>[@<o>]
            if (segment == null) {
              segment = new MediaSegment();
            }

            String[] s = line.split(":");
            s = s[1].split("@");
            if (s.length > 0) {
              segment.setRangeLength(Integer.parseInt(s[0]));
            }
            if (s.length > 1) {
              segment.setRangeStart(Integer.parseInt(s[1]));
            } else {
              List<MediaSegment> segments = mediaPlaylist.getMediaSegments();
              MediaSegment last = segments == null || segments.isEmpty() ? null : segments.get(segments.size() - 1);
              if (last == null) {
                throw new M3u8DownloadException(M3u8DownloadException.ERRNO_PARSE_SUBRANGE_SEGMENT, "subrange error");
              }
              segment.setRangeStart(last.getRangeStart() + last.getRangeLength());
            }
          } else if (line.startsWith(MEDIA_TAG_DISCONTINUITY)) {
            if (segment == null) {
              segment = new MediaSegment();
            }
            segment.setDiscontinuity(true);
          } else if (line.startsWith(MEDIA_TAG_KEY)) {// TODO: 2020-03-27 dhl test
            //#EXT-X-KEY:<attribute-list>
            if (segment == null) {
              segment = new MediaSegment();
            }
            Map<String, String> attrs = attrToMap(line);
            key = new Key();
            key.setMethod(attrs.get(KEY_ATTR_METHOD));
            key.setUri(attrs.get(KEY_ATTR_URI));
            key.setIv(attrs.get(KEY_ATTR_IV));
            key.setKeyformat(attrs.get(KEY_ATTR_KEYFORMAT));
            key.setKeyformatVersions(attrs.get(KEY_ATTR_KEYFORMATVERSIONS));
          }
        } else if (line.trim().isEmpty()) {//blank line
          //ignore
        } else {//uri line
          if (segment != null) {
            segment.setUri(line);
            segment.setKey(key);
            mediaPlaylist.addMediaSegment(segment);
            segment = null;
          }
        }
      }
      return mediaPlaylist;
    } catch (Exception e) {
      return null;
    } finally {
      M3u8Util.close(reader);
    }
  }

}
