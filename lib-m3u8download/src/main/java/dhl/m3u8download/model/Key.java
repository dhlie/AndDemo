package dhl.m3u8download.model;

import java.io.Serializable;

/**
 * Author: duanhl
 * Create: 2020-03-26 17:03
 * Description:
 */
public class Key implements Serializable {

  /**
   * The methods defined are: NONE, AES-128, and SAMPLE-AES.
   */
  private String method;

  private String uri;

  private String iv;

  private String keyformat;

  private String keyformatVersions;

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getIv() {
    return iv;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public String getKeyformat() {
    return keyformat;
  }

  public void setKeyformat(String keyformat) {
    this.keyformat = keyformat;
  }

  public String getKeyformatVersions() {
    return keyformatVersions;
  }

  public void setKeyformatVersions(String keyformatVersions) {
    this.keyformatVersions = keyformatVersions;
  }
}
