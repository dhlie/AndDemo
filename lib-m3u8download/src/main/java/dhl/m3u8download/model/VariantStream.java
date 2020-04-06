package dhl.m3u8download.model;

import java.io.Serializable;

/**
 * Author: duanhl
 * Create: 2020-03-25 16:12
 * Description:
 */
public class VariantStream implements Serializable {
  /**
   * The value is a decimal-integer of bits per second. Every EXT-X-STREAM-INF tag MUST include the BANDWIDTH attribute.
   */
  private int bandwidth;

  /**
   * Every EXT-X-STREAM-INF tag SHOULD include a CODECS attribute.
   */
  private String codecs;

  /**
   * The RESOLUTION attribute is OPTIONAL but is recommended if the Variant Stream includes video.
   */
  private String resolution;

  /**
   * The FRAME-RATE attribute is OPTIONAL but is recommended if the Variant Stream includes video.
   */
  private float frame_rate;

  private String name;

  private String uri;

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getCodecs() {
    return codecs;
  }

  public void setCodecs(String codecs) {
    this.codecs = codecs;
  }

  public String getResolution() {
    return resolution;
  }

  public void setResolution(String resolution) {
    this.resolution = resolution;
  }

  public float getFrameRate() {
    return frame_rate;
  }

  public void setFrameRate(float frame_rate) {
    this.frame_rate = frame_rate;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
