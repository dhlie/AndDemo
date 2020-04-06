package dhl.m3u8download.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: duanhl
 * Create: 2020-03-25 16:06
 * Description:
 */
public class MasterPlaylist extends Playlist {

  private List<VariantStream> variantStreams;

  public void addVariantStream(VariantStream stream) {
    if (stream == null) {
      return;
    }
    if (variantStreams == null) {
      variantStreams = new ArrayList<>();
    }
    variantStreams.add(stream);
  }

  public List<VariantStream> getVariantStreams() {
    return variantStreams;
  }

}
