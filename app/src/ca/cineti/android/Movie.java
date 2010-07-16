package ca.cineti.android;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.github.droidfu.concurrent.BetterAsyncTask;

/**
 * Displays info about a movie.
 */
public class Movie extends Activity {

	static final String MOVIE_ID = Main.PKG_NAME + ".movieID";
	static final String SHOW_TIMES = "showTimes";
	static final String CINEMA_NAME = "cinemaName";
	
	class FetchTask extends BetterAsyncTask<Void, Void, JSONObject>
	{
		private static final String DAY = "day";
		private static final String MOVIE = "http://api.cineti.ca/movie/";
		
		private int id;
		private Map<Day, JSONObject> weekRem;
		
		FetchTask(Context ctx, int id) {
            super(ctx);
            this.id = id;
            this.weekRem = new EnumMap<Day, JSONObject>(Day.class);
        }
		
		/* (non-Javadoc)
         * @see com.github.droidfu.concurrent.BetterAsyncTask#doCheckedInBackground(android.content.Context, ParameterT[])
         */
        @Override
        protected JSONObject doCheckedInBackground(Context context, Void... bleh) throws Exception {
        	JSONObject result = new JSONObject(new DefaultHttpClient().execute(Main.targetHost, 
        														  new HttpGet(MOVIE + Integer.toString(this.id) + ".json"), 
        														  new BasicResponseHandler()));
        	Day today = Day.today();
        	Day counter = today.next();
        	JSONObject futureData;
        	while (counter != today) {
        		String url = MOVIE + Integer.toString(this.id) + ".json?day=" + counter.name();
        		futureData = new JSONObject(new DefaultHttpClient().execute(Main.targetHost, 
						  													new HttpGet(url), 
						  													new BasicResponseHandler()));
        		this.weekRem.put(counter, futureData);
        		counter = counter.next();
        	}
        	return result;
        }

		@Override
		protected void handleError(Context ctx, Exception e) {
			// TODO: Log details and provide useful user feedback. 
			e.printStackTrace();
			MovieData data = new MovieData(ctx, this.id);
			displayInfo(data);
		}

		@Override
		protected void after(Context ctx, JSONObject jsonMovie) {
			MovieData data = new MovieData(ctx, this.id, jsonMovie);
			for (Map.Entry<Day, JSONObject> futureData : this.weekRem.entrySet()) {
				data.absorbShowtimes(futureData.getKey(), futureData.getValue());
			}
			displayInfo(data);
			data.persist(ctx);
		}

		/**
		 * Display info about the movie to the user.
		 * @param data Metadata about the movie.
		 */
		private void displayInfo(MovieData data) {
			ExpandableListView elv = (ExpandableListView) findViewById(R.id.showings);
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(data.getTitle());
			TextView genre = (TextView) findViewById(R.id.genre);
			genre.setText(data.getGenre());
			TextView synopsis =(TextView) findViewById(R.id.synopsis);
			synopsis.setText(data.getSynopsis());
			final List<Map<String, String>> headerData = new ArrayList<Map<String, String>>();
			Day currentDay = Day.today();
			Day someDay = currentDay;
			do {
				Map<String, String> day = new HashMap<String, String>();
				day.put(DAY, someDay.toString());
				headerData.add(day);
				someDay = someDay.next();
			} while (someDay != currentDay);
			final List<List<Map<String, String>>> contentData = data.getScreenings();
			SimpleExpandableListAdapter expListAdapter = 
				new SimpleExpandableListAdapter(Movie.this, 
												 headerData, 
												 android.R.layout.simple_expandable_list_item_1, 
												 new String[] { DAY }, 
												 new int[] {android.R.id.text1}, 
												 contentData, 
												 R.layout.daily_showings, 
												 new String[] { Movie.CINEMA_NAME, Movie.SHOW_TIMES }, 
												 new int[] { R.id.cinemaName, R.id.showTimes });
			elv.setAdapter(expListAdapter);
			elv.expandGroup(0);
			setProgressBarIndeterminateVisibility(false);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		int id = this.getIntent().getIntExtra(MOVIE_ID, 0);
		setContentView(R.layout.movie);
		setProgressBarIndeterminateVisibility(true);
		new FetchTask(getApplicationContext(), id).execute();
	}

}
