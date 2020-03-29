package dhl.m3u8download.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: duanhl
 * Create: 2020-03-25 16:06
 * Description:
 */
public class MasterPlaylist extends Playlist {

	private String url;
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

	public String getPlaylistUrl(VariantStream stream) {
		String streamUri = stream.getUri();
		if (streamUri.startsWith("http")) {
			return streamUri;
		} else if (streamUri.startsWith("/")) {
			Uri masterUri = Uri.parse(url);
			Uri subUri = new Uri.Builder().scheme(masterUri.getScheme()).authority(masterUri.getAuthority())
							.path(streamUri).build();
			return subUri.toString();
		} else {
			String streamUrl = url.substring(0, url.lastIndexOf('/') + 1);
			streamUrl = streamUrl + streamUri;
			return streamUrl;
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<VariantStream> getVariantStreams() {
		return variantStreams;
	}

}
