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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;

/**
 * Encapsulates all the metadata about a movie.
 */
public class MovieData {
	private static final String LARGE = "large";
	private static final String SIZE = "size";
	private static final String POSTERS = "posters";
	private static final String NAME = "name";
	private static final String THEATRES = "theaters";
	private static final String PLOT = "plot";
	private static final String TITLE = "title";
	private static final String SYNOPSIS = "synopsis";
	private static final String GENRE = "genre";

	static final String EXT_JPEG = ".jpg";

	
	private int id; // 5 digit ID number used by Cineti.ca
	private Bitmap thumbnail;
	private String title;
	private String synopsis;
	private String genre;
	// Maps from a day to a list of theatres & showtimes.
	private Map<String, List<Map<String, String>>> showings;
	
	/**
	 * Extract the showtimes for a movie from a JSON object.
	 * @param ctx App context
	 * @param id movie ID num
	 * @param json JSON object containing movie details
	 */
	public MovieData(Context ctx, int id, JSONObject json) {
		this.id = id;
		try {
			this.title = json.getString(TITLE);
			this.genre = json.getString(GENRE);
			this.synopsis = json.getString(PLOT);
			this.showings = new HashMap<String, List<Map<String, String>>>();
			
			// Parse the cinema name and screening times
			List<Map<String, String>> screenings = extractScreenings(json);
			this.showings.put(Day.today().name(), screenings);
			
			// Load the image. 
			String filename = Integer.toString(id) + EXT_JPEG;
			try {
				InputStream in = ctx.openFileInput(filename);
				this.thumbnail = BitmapFactory.decodeStream(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray posters = json.getJSONArray(POSTERS);
			for (int i = 0; i< posters.length(); i++) {
				if (posters.getJSONObject(i).getString(SIZE).equals(LARGE)) {
					break;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void absorbShowtimes(Day day, JSONObject json) {
		try {
			List<Map<String, String>> screenings = extractScreenings(json);
			this.showings.put(day.name(), screenings);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	private List<Map<String, String>> extractScreenings(JSONObject json)
			throws JSONException {
		Map<Cinema, List<Time>> today = new EnumMap<Cinema, List<Time>>(Cinema.class);
		JSONArray cinemas = json.getJSONArray(THEATRES);
		for (int i = 0; i < cinemas.length(); i++) {
			JSONObject cinemaDetails = cinemas.getJSONObject(i);
			String cinemaName = cinemaDetails.getString(NAME);
			try {
				Cinema cinema = Cinema.parseString(cinemaName);
				List<Time> showTimes = parseShowtimes(cinemaDetails);
				if (showTimes.size() > 0) {
					today.put(cinema, showTimes);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Format and store the screening times as strings
		List<Map<String, String>> screenings = new LinkedList<Map<String, String>>();
		for (Map.Entry<Cinema, List<Time>> c : today.entrySet()) {
			Map<String, String> deets = new HashMap<String, String>();
			StringBuffer times = new StringBuffer();
			for (Time t : c.getValue()) {
				times.append(t.format("%l:%M%p "));
			}
			deets.put(Movie.CINEMA_NAME, c.getKey().toString() + ": ");
			deets.put(Movie.SHOW_TIMES, times.toString());
			screenings.add(deets);
		}
		return screenings;
	}

	/**
	 * Utility method to parse out the schedule of a given movie at a specific cinema from JSON data.
	 * @param cinemaDetails JSON object containing the schedule to be parsed.
	 * @return
	 * @throws JSONException
	 */
	private List<Time> parseShowtimes(JSONObject cinemaDetails)
			throws JSONException {
		List<Time> showTimes = new LinkedList<Time>();
		JSONArray times = cinemaDetails.getJSONArray("times");
		if (times.length() == 0) {
			Log.e("Cineti", "No times could be extracted from the JSON data: " + cinemaDetails);
		} else {
			String date = cinemaDetails.getString("date");
			for (int j = 0; j < times.length(); j++) {
				String showTime = times.getString(j);
				Time screening = new Time();
				screening.parse3339(date + 'T' + showTime);
				showTimes.add(screening);
			}
		}
		return showTimes;
	}
	
	/**
	 * Populate the ID, title, thumbnail & showtimes from a JSON object.
	 * @param ctx App context for loading cached thumbnail image.
	 * @param json JSON data containing movie ID, title & schedule for a particular cinema. 
	 * @param cinema The cinema for which to retrieve the showtimes.
	 */
	public MovieData(Context ctx, JSONObject json, Cinema cinema) {
		this(ctx, json);
		if (this.thumbnail == null) {
			this.thumbnail = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.thumbnail);
		}
		// Load schedule.
		SharedPreferences cachedData = ctx.getSharedPreferences(Integer.toString(this.id), 0);
		if (!loadCachedShowings(cachedData) ||
			!this.showings.containsKey(Day.today().name() + '@' + cinema.toString())) {
			// No cached schedule so extract from JSON.
			this.showings = new HashMap<String, List<Map<String, String>>>();
			try {
				List<Time> showTimes = parseShowtimes(json);
				List<Map<String, String>> screenings = new LinkedList<Map<String, String>>();
				Map<String, String> deets = new HashMap<String, String>();
				StringBuffer times = new StringBuffer();
				for (Time t : showTimes) {
					times.append(t.format("%l:%M%p "));
				}
				deets.put(Movie.CINEMA_NAME, cinema.toString() + ": ");
				deets.put(Movie.SHOW_TIMES, times.toString());
				screenings.add(deets);
				this.showings.put(Day.today().name(), screenings);
				this.persist(ctx);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Populate the ID, title and thumbnail from a JSON object.
	 * @param ctx App context for loading cached thumbnail image.
	 * @param json JSON data containing movie ID, title & thumbnail URl.
	 * @throws NumberFormatException
	 */
	public MovieData(Context ctx, JSONObject json)
			throws NumberFormatException {
		try {
			// Parse out the movie's ID #
			String spime = json.getString("href");
			String idNum = spime.substring(spime.lastIndexOf('/') + 1);
			this.id = Integer.parseInt(idNum);
			// Fetch the title and thumbnail poster image
			this.title = Html.fromHtml(json.getString("title")).toString();
			InputStream in;
			String filename = idNum + EXT_JPEG;
			try {
				in = ctx.openFileInput(filename);
			} catch (FileNotFoundException e) {
					// Load thumbnail from server.
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
	 * Populate the ID, title and thumbnail from a Map entry mapping ID to title. Also try to load cached schedule.
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
		loadCachedShowings(ctx.getSharedPreferences(Integer.toString(this.id), 0));
		if (this.thumbnail == null) {
			this.thumbnail = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.thumbnail);
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
		ed.putString(TITLE, this.title);
		if (this.genre != null) {
			ed.putString(GENRE, this.genre);
		}
		if (this.synopsis != null) {
			ed.putString(SYNOPSIS, this.synopsis);
		}
		Day today = Day.today();
		Day someDay = today;
		do {
			for (Map<String, String> showTimes : this.showings.get(someDay.name())) {
				String cinemaName = showTimes.get(Movie.CINEMA_NAME);
				ed.putString(someDay.name() + '@' + cinemaName.substring(0, cinemaName.length() - 2), 
							 showTimes.get(Movie.SHOW_TIMES));
			}
			someDay = someDay.next();
		} while (someDay != today);
		ed.commit();
	}

	/**
	 * @param cachedData
	 */
	private boolean loadCachedShowings(SharedPreferences cachedData) {
		this.showings = new HashMap<String, List<Map<String, String>>>();
		List<Map<String, String>> today = new LinkedList<Map<String, String>>();
		String currentDay = Day.today().name();
		for (Cinema cinema : Cinema.array) {
			String showTimes = cachedData.getString(currentDay + '@' + cinema.toString(), "");
			if (showTimes.length() > 0) {
				Map<String, String> deets = new HashMap<String, String>();
				deets.put(Movie.CINEMA_NAME, cinema.toString() + ": ");
				deets.put(Movie.SHOW_TIMES, showTimes);
				today.add(deets);
			}
		}
		this.showings.put(currentDay, today);
		return today.size() > 0;
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
		loadCachedShowings(cachedData);
	}

	public List<Map<String, String>> getScreenings() {
		return this.showings.get(Day.today().name());
	}
	
	public String getScreenings(Cinema cinema) {
		List<Map<String, String>> all = this.showings.get(Day.today().name());
		for (Map<String, String> schedule : all) {
			if (schedule.get(Movie.CINEMA_NAME).equals(cinema.toString() + ": ")) {
				return schedule.get(Movie.SHOW_TIMES);
			}
		}
		Log.w("cineti", "No showings of " + this.title + " found at " + cinema.toString() + " for today.");
		return "No showings today.";
	}
}
