package ca.cineti.android;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;

/**
 * Displays information about a particular cinema, such as the list of movies playing at it.
 */
public class Screenings extends ListActivity {

	public static final String CINEMA_NAME = "cinema name";
	
	private List<MovieData> movies;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.movies = new LinkedList<MovieData>();
		String cinemaName = this.getIntent().getStringExtra(CINEMA_NAME);
		String url = "http://api.cineti.ca/theater/" + cinemaName + ".json";
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);
		try {
			Cinema cinema = Cinema.parseString(cinemaName);
			try {
				JSONObject cinemaInfo =
					new JSONObject(new DefaultHttpClient().execute(Main.targetHost, 
																	new HttpGet(url), 
																	new BasicResponseHandler()));
				JSONArray jsonScreenings = cinemaInfo.getJSONArray("movies");
				for (int i = jsonScreenings.length() - 1; i >= 0; i--) {
					JSONObject screeningInfo = jsonScreenings.getJSONObject(i);
					this.movies.add(new MovieData(this, screeningInfo, cinema));
				}
				this.setListAdapter(new ScreeningsAdapter(this, R.layout.screenings, this.movies, cinema));
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// Offline so use cached data.
				Map<String, ?> movieTitles = this.getSharedPreferences(cinema.name(), 0).getAll();;
		        for (Map.Entry<String, ?> movieData : movieTitles.entrySet()) {
		        	this.movies.add(new MovieData(this, movieData));
		        }
		        this.setListAdapter(new ScreeningsAdapter(this, R.layout.screenings, this.movies, cinema));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setProgressBarIndeterminateVisibility(false);
			this.getListView().setTextFilterEnabled(true);
			
			// Cache the list of movie IDs for this cinema.
			Editor ed = this.getSharedPreferences(cinema.name(), 0).edit();
			ed.clear();
			for (MovieData movie : this.movies) {
				if (movie.getThumbnail() != null) {
					ed.putString(Integer.toString(movie.getId()), movie.getTitle());
				}
			}
			ed.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
