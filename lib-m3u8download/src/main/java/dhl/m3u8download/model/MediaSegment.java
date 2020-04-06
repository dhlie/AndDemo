package dhl.m3u8download.model;

import java.io.Serializable;

/**
 * Author: duanhl
 * Create: 2020-03-26 16:54
 * Description:
 */
public class MediaSegment implements Serializable {

  /**
   * REQUIRED 格式：#EXTINF:<duration>,[<title>]
   */
  private float duration;

  /**
   * #EXT-X-BYTERANGE:<n>[@<o>]
   */
  private int rangeLength;

  private int rangeStart;

  /**
   * #EXT-X-DISCONTINUITY
   */
  private boolean discontinuity;

  /**
   * #EXT-X-KEY:<attribute-list>
   */
  private Key key;

  private String uri;

  public float getDuration() {
    return duration;
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }

  public int getRangeLength() {
    return rangeLength;
  }

  public void setRangeLength(int rangeLength) {
    this.rangeLength = rangeLength;
  }

  public int getRangeStart() {
    return rangeStart;
  }

  public void setRangeStart(int rangeStart) {
    this.rangeStart = rangeStart;
  }

  public boolean isDiscontinuity() {
    return discontinuity;
  }

  public void setDiscontinuity(boolean discontinuity) {
    this.discontinuity = discontinuity;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
