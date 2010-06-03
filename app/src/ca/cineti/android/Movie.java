package ca.cineti.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;





import com.github.droidfu.concurrent.BetterAsyncTask;



import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

/**
 *
 */
public class Movie extends Activity {

	static final String MOVIE_ID = Main.PKG_NAME + ".movieID";
	
	private static final String DAY = "day";
	
	private class FetchTask extends BetterAsyncTask<Void, Void, JSONObject>
	{
		private static final String MOVIE = "http://api.cineti.ca/movie/";
		private int id;

		FetchTask(Context ctx, int id) {
            super(ctx);
            this.id = id;
        }
		
		/* (non-Javadoc)
         * @see com.github.droidfu.concurrent.BetterAsyncTask#doCheckedInBackground(android.content.Context, ParameterT[])
         */
        @Override
        protected JSONObject doCheckedInBackground(Context context, Void... bleh) throws Exception {
        	return new JSONObject(new DefaultHttpClient().execute(Main.targetHost, 
        														  new HttpGet(MOVIE + Integer.toString(this.id) + ".json"), 
        														  new BasicResponseHandler()));
        }

		@Override
		protected void handleError(Context ctx, Exception e) {
			// TODO: Log details and provide useful user feedback. 
			e.printStackTrace();
		}

		@Override
		protected void after(Context ctx, JSONObject jsonMovie) {
			ExpandableListView elv = (ExpandableListView) findViewById(R.id.showings);
			MovieData data = new MovieData(ctx, this.id, jsonMovie);
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(data.title);
			TextView synopsis =(TextView) findViewById(R.id.synopsis);
			synopsis.setText(data.synopsis);
			final List<Map<String, String>> headerData = new ArrayList<Map<String, String>>();
			final List<List<Map<String, Object>>> contentData = new ArrayList<List<Map<String, Object>>>();
			SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(Movie.this, 
																						 headerData, 
																						 android.R.layout.simple_expandable_list_item_1, 
																						 new String[] { DAY }, 
																						 new int[] {android.R.id.text1}, 
																						 contentData, 
																						 R.layout.daily_showings, 
																						 new String[] { "cinemaName", "showTimes" }, 
																						 new int[] { R.id.cinemaName, R.id.showTimes });
			elv.setAdapter(expListAdapter);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int id = this.getIntent().getIntExtra(MOVIE_ID, 0);
		setContentView(R.layout.movie);
		new FetchTask(getApplicationContext(), id).execute();
	}

}
