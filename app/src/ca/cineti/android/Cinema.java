package ca.cineti.android;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

/**
 * Displays information about a particular cinema, such as the list of movies playing at it.
 */
public class Cinema extends ListActivity {

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
			CinemaData cinema = CinemaData.parseString(cinemaName);
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
		} catch (UnknownHostException e) {
			//TODO: offline so use cached data.
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setProgressBarIndeterminateVisibility(false);
		this.getListView().setTextFilterEnabled(true);
	}

}
