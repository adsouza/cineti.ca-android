package ca.cineti.android;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;
import android.util.Log;

/**
 * Encapsulates all the metadata about a movie.
 */
public class MovieData {
	private static final String TITLE = "title";
	private static final String SYNOPSIS = "synopsis";
	private static final String GENRE = "genre";

	static final String EXT_JPEG = ".jpg";

	
	private int id; // 5 digit ID number used by Cineti.ca
	private Bitmap thumbnail;
	private String title;
	private String synopsis;
	private String genre;
	// Maps from a date string in yyyy-mm-dd format to a list of theatres & showtimes.
	private Map<String, List<Map<String, String>>> showings;
	
	public MovieData(Context ctx, int id, JSONObject json) {
		this.id = id;
		try {
			this.title = json.getString("title");
			this.genre = json.getString(GENRE);
			this.synopsis = json.getString("plot");
			Map<CinemaData, List<Time>> today = new EnumMap<CinemaData, List<Time>>(CinemaData.class);
			// Parse the cinema name and screening times
			JSONArray cinemas = json.getJSONArray("theaters");
			for (int i = 0; i < cinemas.length(); i++) {
				JSONObject cinemaDetails = cinemas.getJSONObject(i);
				String cinemaName = cinemaDetails.getString("name");
				try {
					CinemaData cinema = CinemaData.parseString(cinemaName);
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
			// Format and store the screening times as strings
			this.showings = new HashMap<String, List<Map<String, String>>>();
			List<Map<String, String>> screenings = new LinkedList<Map<String, String>>();
			for (Map.Entry<CinemaData, List<Time>> c : today.entrySet()) {
				Map<String, String> deets = new HashMap<String, String>();
				StringBuffer times = new StringBuffer();
				for (Time t : c.getValue()) {
					times.append(t.format("%l:%M%p "));
				}
				deets.put(Movie.FetchTask.CINEMA_NAME, c.getKey().toString() + ": ");
				deets.put(Movie.FetchTask.SHOW_TIMES, times.toString());
				screenings.add(deets);
			}
			this.showings.put(new SimpleDateFormat("E").format(new Date()), screenings);
			
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
	 * Caches the metadata in local storage for later retrieval while offline.
	 * @param ctx App context to use for storing data.
	 */
	public void persist(Context ctx) {
		Editor ed = ctx.getSharedPreferences(Integer.toString(this.id), 0).edit();
		ed.putString(TITLE, title);
		ed.putString(GENRE, genre);
		ed.putString(SYNOPSIS, synopsis);
		String today = new SimpleDateFormat("E").format(new Date());
		for (Map<String, String> showTimes : this.showings.get(today)) {
			String cinemaName = showTimes.get(Movie.FetchTask.CINEMA_NAME);
			ed.putString(today + '@' + cinemaName.substring(0, cinemaName.length() - 2), 
						 showTimes.get(Movie.FetchTask.SHOW_TIMES));
		}
		ed.commit();
	}

	/**
	 * Instantiates a MovieData object for a specific movie using cached data.
	 * @param ctx App context
	 * @param id Movie ID number
	 */
	public MovieData(Context ctx, int id) {
		this.id = id;
		SharedPreferences cachedData = ctx.getSharedPreferences(Integer.toString(id), 0);
	    // Retrieve cached data
		this.genre = cachedData.getString(GENRE, "generic");
		this.title = cachedData.getString(TITLE, "Untitled");
		this.synopsis = cachedData.getString(SYNOPSIS, "Not much happens.");
		this.showings = new HashMap<String, List<Map<String, String>>>();
		List<Map<String, String>> today = new LinkedList<Map<String, String>>();
		String currentDay = new SimpleDateFormat("E").format(new Date());
		for (CinemaData cinema : CinemaData.array) {
			String showTimes = cachedData.getString(currentDay + '@' + cinema.toString(), "");
			if (showTimes.length() > 0) {
				Map<String, String> deets = new HashMap<String, String>();
				deets.put(Movie.FetchTask.CINEMA_NAME, cinema.toString() + ": ");
				deets.put(Movie.FetchTask.SHOW_TIMES, showTimes);
				today.add(deets);
			}
		}
		this.showings.put(currentDay, today);
	}

	public List<Map<String, String>> getScreenings() {
		return this.showings.get(new SimpleDateFormat("E").format(new Date()));
	}
}
