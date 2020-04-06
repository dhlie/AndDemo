package dhl.m3u8download;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: duanhl
 * Create: 2020-04-02 15:26
 * Description:
 */
public class FlingRequest {

  private final Set<String> flingRequests = new HashSet<>();

  public void addRequest(String url) {
    synchronized (flingRequests) {
      flingRequests.add(url);
    }
  }

  public void removeRequest(String url) {
    synchronized (flingRequests) {
      flingRequests.remove(url);
    }
  }

  public boolean isFlingAndMakeInFling(String url) {
    synchronized (flingRequests) {
      boolean isFling = flingRequests.contains(url);
      if (!isFling) {
        flingRequests.add(url);
      }
      return isFling;
    }
  }
}
