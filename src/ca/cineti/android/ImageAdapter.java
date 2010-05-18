package ca.cineti.android;

import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.github.droidfu.concurrent.BetterAsyncTask;


/**
 * Provides image data for use in the Movies GridView.
 */
public class ImageAdapter extends BaseAdapter {
	static final HttpHost targetHost = new HttpHost("api.cineti.ca", 80, "http");
	private static final String TITLES = "cached movie titles";
	Movies mOwner;
	MovieData[] movies;
	int mNumLoaded;
	
	private class RefreshTask extends BetterAsyncTask<Void, Void, Void>
	{
		private static final String MOVIES = "http://api.cineti.ca/movies.json";

		public RefreshTask(Context ctx) {
            super(ctx);
        }
		
		/* (non-Javadoc)
         * @see com.github.droidfu.concurrent.BetterAsyncTask#doCheckedInBackground(android.content.Context, ParameterT[])
         */
        @Override
        protected Void doCheckedInBackground(Context context, Void... blah) throws Exception {
    		JSONArray jsonMovies = new JSONArray(new DefaultHttpClient().execute(targetHost, new HttpGet(MOVIES), new BasicResponseHandler()));
    		ImageAdapter.this.mNumLoaded = 0;
			ImageAdapter.this.movies = new MovieData[jsonMovies.length()];
			int opposingFinger = ImageAdapter.this.movies.length - 1;
			int counter = 0;
			while (ImageAdapter.this.mNumLoaded <= opposingFinger) {
				MovieData movie = new MovieData(jsonMovies.getJSONObject(counter));
				counter++;
				if (movie.thumbnail != null) {
					ImageAdapter.this.movies[ImageAdapter.this.mNumLoaded] = movie;
					ImageAdapter.this.mNumLoaded++;
				} else {
					ImageAdapter.this.movies[opposingFinger] = movie;
					opposingFinger--;
				}
			}
			return null;
        }

		@Override
		protected void handleError(Context ctx, Exception e) {
			// TODO: Log details and provide useful user feedback. 
			e.printStackTrace();
		}

		@Override
		protected void after(Context ctx, Void blah) {
			ImageAdapter.this.mOwner.refresh();
		}
	}
	
	/**
	 * Construct a new ImageAdapter.
	 */
	public ImageAdapter(Movies owner) {
        this.mOwner = owner;
        // Retrieve data from cache
        Map<String, ?> movieTitles = this.mOwner.getSharedPreferences(TITLES, 0).getAll();
        this.mNumLoaded = movieTitles.size();
        this.movies = new MovieData[this.mNumLoaded];
        // Initialise app with cached data
        // Refresh data from server
        new RefreshTask(this.mOwner.getApplicationContext()).execute();
    }

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return this.mNumLoaded;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * Create a new ImageView for each item referenced by the Adapter
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialise some attributes
            imageView = new ImageView(this.mOwner);
            imageView.setLayoutParams(new GridView.LayoutParams(95, 140));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
		imageView.setImageBitmap(this.movies[position].thumbnail);
        return imageView;
	}

}
