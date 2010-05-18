package ca.cineti.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Encapsulates all the metadata about a movie.
 */
public class MovieData {
	int id; // 5 digit ID number used by Cineti.ca
	Bitmap thumbnail;
	String title;
	
	public MovieData(JSONObject json) {
		try {
			this.title = json.getJSONArray("@children").getJSONObject(0).getString("@text");
			String url = json.getJSONArray("@children").getJSONObject(1).getJSONObject("@attributes").getString("href");
			URL thumb = new URL(url);
			InputStream is = thumb.openStream();
			this.thumbnail = BitmapFactory.decodeStream(is);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
