package dhl.m3u8download.model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Author: duanhl
 * Create: 2020-03-28 16:37
 * Description:
 */
public class Playlist implements Serializable {

  protected String url;

  public String getResUrl(String resUri) {
    if (resUri == null || resUri.length() == 0) {
      return resUri;
    }
    if (resUri.startsWith("http")) {
      return resUri;
    } else if (resUri.startsWith("/")) {
      Uri playlistUri = Uri.parse(url);
      Uri subUri = new Uri.Builder().scheme(playlistUri.getScheme()).authority(playlistUri.getAuthority())
              .path(resUri).build();
      return subUri.toString();
    } else {
      String streamUrl = url.substring(0, url.lastIndexOf('/') + 1);
      streamUrl = streamUrl + resUri;
      return streamUrl;
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
