package ca.cineti.android;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;

enum Cinema {
	AMC, Scotia, Beaubien, Parc, Latin;
}

/**
 * Encapsulates all the metadata about a movie.
 */
public class MovieData {
	int id; // 5 digit ID number used by Cineti.ca
	Bitmap thumbnail;
	String title;
	String synopsis;
	private SortedSet<Map<Cinema, List<Time>>> showings;
	
	public MovieData(Context ctx, int id, JSONObject json) {
		this.id = id;
		try {
			this.title = json.getString("title");
			this.synopsis = json.getString("plot");
			this.showings = new TreeSet<Map<Cinema, List<Time>>>();
			Map<Cinema, List<Time>> today = new EnumMap<Cinema, List<Time>>(Cinema.class);
			this.showings.add(today);
			Cinema cinema = Cinema.AMC;
			List<Time> showTimes = new LinkedList<Time>();
			today.put(cinema, showTimes );
			String filename = Integer.toString(id) + ".jpg";
			try {
				InputStream in = ctx.openFileInput(filename);
				this.thumbnail = BitmapFactory.decodeStream(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray posters = json.getJSONArray("posters");
			for (int i = 0; i< posters.length(); i++) {
				if (posters.getJSONObject(i).getString("size").equals("large")) {
					break;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Populate the ID, title and thumbnail from a JSON object.
	 * @param ctx App context for loading cached thumbnail image.
	 * @param json JSON data containing movie ID, title & thumbnail URl. 
	 */
	public MovieData(Context ctx, JSONObject json) {
		try {
			// Parse out the movie's ID #
			String spime = json.getString("href");
			String idNum = spime.substring(spime.lastIndexOf('/') + 1);
			this.id = Integer.parseInt(idNum);
			// Fetch the title and thumbnail poster image
			this.title = json.getString("title");
			InputStream in;
			String filename = idNum + ".jpg";
			try {
				in = ctx.openFileInput(filename);
			} catch (FileNotFoundException e) {
				String url = json.getString("thumbnail");
				URL thumb = new URL(url);
				in = thumb.openStream();
				// Save the image to a file
				FileOutputStream out = ctx.openFileOutput(filename, 0);
				byte[] buffer = MovieData.toByteArray(in);
				out.write(buffer);
				out.flush();
				in.close();
				out.close();
				// Reopen the image URL to reread it.
				in = thumb.openStream();
			}
			this.thumbnail = BitmapFactory.decodeStream(in);
			if (this.thumbnail == null) {
				ctx.deleteFile(filename);
			}
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

	private static byte[] toByteArray(InputStream in) throws IOException {
		byte[] buffer = new byte[16384];
	    int bytesRead;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    while ((bytesRead = in.read(buffer)) != -1) {
	        output.write(buffer, 0, bytesRead);
	    }
	    output.flush();
	    return output.toByteArray();
	}

	/**
	 * Populate the ID, title and thumbnail from a Map entry mapping ID to title.
	 * @param ctx App context for loading cached thumbnail image.
	 * @param movieData Map entry with ID as key and title as value.
	 */
	MovieData(Context ctx, Entry<String, ?> movieData) {
		this.id = Integer.parseInt(movieData.getKey());
		this.title = (String)movieData.getValue();
		String filename = movieData.getKey() + ".jpg";
		try {
			InputStream in = ctx.openFileInput(filename);
			this.thumbnail = BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getId() {
		return this.id;
	}

	public Bitmap getThumbnail() {
		return this.thumbnail;
	}

	public String getTitle() {
		return this.title;
	}
}
