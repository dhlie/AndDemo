package dhl.m3u8download.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: duanhl
 * Create: 2020-03-25 16:11
 * Description:
 */
public class MediaPlaylist extends Playlist {

	private String url;

	/**
	 * #EXT-X-TARGETDURATION:<s>
	 */
	private int targetDuration;

	/**
	 * #EXT-X-MEDIA-SEQUENCE:<number>
	 */
	private int sequence;

	/**
	 * The client MUST periodically reload a Media Playlist file to learn
	 * what media is currently available, unless it contains an EXT-X
	 * -PLAYLIST-TYPE tag with a value of VOD, or a value of EVENT and the
	 * EXT-X-ENDLIST tag is also present.
	 */
	private boolean isVod;

	private List<MediaSegment> mediaSegments;

	private Float duration;

	public String getMediaSegmentUrl(MediaSegment ts) {
		String tsUri = ts.getUri();
		if (tsUri.startsWith("http")) {
			return tsUri;
		} else if (tsUri.startsWith("/")) {
			Uri playlistUri = Uri.parse(url);
			Uri subUri = new Uri.Builder().scheme(playlistUri.getScheme()).authority(playlistUri.getAuthority())
							.path(tsUri).build();
			return subUri.toString();
		} else {
			String streamUrl = url.substring(0, url.lastIndexOf('/') + 1);
			streamUrl = streamUrl + tsUri;
			return streamUrl;
		}
	}

	public void addMediaSegment(MediaSegment ts) {
		if (ts == null) {
			return;
		}
		if (mediaSegments == null) {
			mediaSegments = new ArrayList<>();
		}
		mediaSegments.add(ts);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<MediaSegment> getMediaSegments() {
		return mediaSegments;
	}

	public int getTargetDuration() {
		return targetDuration;
	}

	public void setTargetDuration(int targetDuration) {
		this.targetDuration = targetDuration;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public boolean isVod() {
		return isVod;
	}

	public void setVod(boolean vod) {
		isVod = vod;
	}

	public float getDuration() {
		if (duration != null) {
			return duration;
		}
		float dur = 0f;
		if (mediaSegments != null) {
			for (MediaSegment segment : mediaSegments) {
				dur += segment.getDuration();
			}
			duration = dur;
		}
		return dur;
	}

}
