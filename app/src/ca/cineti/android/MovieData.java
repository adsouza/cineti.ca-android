package ca.cineti.android;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;
import android.util.Log;

enum Cinema {
	AMC("AMC Forum 22"), 
	Scotia("Banque Scotia (Paramount)"), 
	Beaubien("Cinema Beaubien"), 
	Parc("Cinema du Parc"), 
	Latin("Quartier Latin");
	
	private static Cinema[] array = { AMC, Scotia,  Beaubien, Parc, Latin };
	
	private String fullName;
	
	private Cinema(String longForm) {
		this.fullName = longForm;
	}
	
	public String toString() {
		return this.fullName;
	}
	
	static Cinema parseString(String ascii) throws Exception {
		for (Cinema c : array) {
			if (c.fullName.equals(ascii)) {
				return c;
			}
		}
		throw new Exception("Unrecognized cinema name: " + ascii);
	}
}

/**
 * Encapsulates all the metadata about a movie.
 */
public class MovieData {
	static final String EXT_JPEG = ".jpg";
	
	private int id; // 5 digit ID number used by Cineti.ca
	private Bitmap thumbnail;
	private String title;
	private String synopsis;
	private List<Map<Cinema, List<Time>>> showings;
	private String genre;
	
	public MovieData(Context ctx, int id, JSONObject json) {
		this.id = id;
		try {
			this.title = json.getString("title");
			this.genre = json.getString("genre");
			this.synopsis = json.getString("plot");
			this.showings = new LinkedList<Map<Cinema, List<Time>>>();
			Map<Cinema, List<Time>> today = new EnumMap<Cinema, List<Time>>(Cinema.class);
			this.showings.add(today);
			// Parse the cinema name and screening times
			JSONArray cinemas = json.getJSONArray("theaters");
			for (int i = 0; i < cinemas.length(); i++) {
				JSONObject cinemaDetails = cinemas.getJSONObject(i);
				String cinemaName = cinemaDetails.getString("name");
				try {
					Cinema cinema = Cinema.parseString(cinemaName);
					List<Time> showTimes = new LinkedList<Time>();
					JSONArray times = cinemaDetails.getJSONArray("times");
					if (times.length() == 0) {
						Log.e("Cineti", "No times could be extracted from the JSON data: " + cinemaDetails);
						continue;
					}
					String date = cinemaDetails.getString("date");
					for (int j = 0; j < times.length(); j++) {
						String showTime = times.getString(j);
						Time screening = new Time();
						screening.parse3339(date + 'T' + showTime);
						showTimes.add(screening);
					}
					today.put(cinema, showTimes);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String filename = Integer.toString(id) + EXT_JPEG;
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
			String filename = idNum + EXT_JPEG;
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
		String filename = movieData.getKey() + EXT_JPEG;
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

	/**
	 * @return the genre
	 */
	public String getGenre() {
		return this.genre;
	}

	/**
	 * @return the synopsis
	 */
	public String getSynopsis() {
		return this.synopsis;
	}

	/**
	 * @return the showings
	 */
	public List<Map<Cinema, List<Time>>> getShowings() {
		return this.showings;
	}

	public List<Map<String, String>> formatScreenings(int dayIndex) {
		List<Map<String, String>> cinemas = new LinkedList<Map<String, String>>();
		Map<Cinema, List<Time>> screeningsToday = this.getShowings().get(dayIndex);
		for (Map.Entry<Cinema, List<Time>> c : screeningsToday.entrySet()) {
			Map<String, String> deets = new HashMap<String, String>();
			StringBuffer times = new StringBuffer();
			for (Time t : c.getValue()) {
				times.append(t.format("%l:%M%p "));
			}
			deets.put(Movie.FetchTask.CINEMA_NAME, c.getKey().toString() + ": ");
			deets.put(Movie.FetchTask.SHOW_TIMES, times.toString());
			cinemas.add(deets);
		}
		return cinemas;
	}
}
