package ca.cineti.android;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
	private static final String TITLES = "cached movie titles";
	Movies mOwner;
	MovieData[] movies;
	int mNumLoaded;
	
	private class ImgLoadTask extends BetterAsyncTask<JSONArray, Void, MovieData>
	{

		private int opposingIndex;
		private JSONArray[] jsonMovies;
		private Set<Integer> losers;

		ImgLoadTask(Context ctx, int opposingIndex, Set<Integer> stale) {
			super(ctx);
			this.opposingIndex = opposingIndex;
			this.losers = stale;
		}
		
		/* (non-Javadoc)
         * @see com.github.droidfu.concurrent.BetterAsyncTask#doCheckedInBackground(android.content.Context, ParameterT[])
         */
        @Override
        protected MovieData doCheckedInBackground(Context context, JSONArray... jsonMoviesData) throws Exception {
			assert jsonMoviesData.length == 1;
			this.jsonMovies = jsonMoviesData;
			int pos = ImageAdapter.this.mNumLoaded + ImageAdapter.this.movies.length - (this.opposingIndex + 1);
			return new MovieData(context, jsonMoviesData[0].getJSONObject(pos));
        }

		@Override
		protected void after(Context ctx, MovieData film) {
			if (film.getThumbnail() != null) {
				ImageAdapter.this.movies[ImageAdapter.this.mNumLoaded] = film;
				ImageAdapter.this.mNumLoaded++;
				this.losers.remove(film.getId());
			} else {
				ImageAdapter.this.movies[this.opposingIndex] = film;
				this.opposingIndex--;
			}
			if (ImageAdapter.this.mNumLoaded <= this.opposingIndex) {
				new ImgLoadTask(this.getCallingContext(), this.opposingIndex, this.losers).execute(this.jsonMovies);
			} else {
				// Save the current movie IDs and titles in cache for offline use.
				Editor ed = ImageAdapter.this.mOwner.getSharedPreferences(TITLES, 0).edit();
				ed.clear();
				for (MovieData movie : ImageAdapter.this.movies) {
					if (movie.getThumbnail() != null) {
						ed.putString(Integer.toString(movie.getId()), movie.getTitle());
					}
				}
				ed.commit();
				// delete image files for movies that are no longer playing
				for (int id : this.losers) {
					ctx.deleteFile(String.valueOf(id) + ".jpg");
				}
			}
			ImageAdapter.this.notifyDataSetChanged();
		}

		@Override
		protected void handleError(Context ctx, Exception e) {
			e.printStackTrace();
		}
	
	}
	
	private class RefreshTask extends BetterAsyncTask<Void, Void, JSONArray>
	{
		private static final String MOVIES = "http://api.cineti.ca/movies.json";

		RefreshTask(Context ctx) {
            super(ctx);
        }
		
		/* (non-Javadoc)
         * @see com.github.droidfu.concurrent.BetterAsyncTask#doCheckedInBackground(android.content.Context, ParameterT[])
         */
        @Override
        protected JSONArray doCheckedInBackground(Context context, Void... blah) throws Exception {
        	return new JSONArray(new DefaultHttpClient().execute(Main.targetHost, new HttpGet(MOVIES), new BasicResponseHandler()));
        }

		@Override
		protected void handleError(Context ctx, Exception e) {
			// TODO: Log details and provide useful user feedback. 
			e.printStackTrace();
			// Retrieve data from cache
	        Map<String, ?> movieTitles = ImageAdapter.this.mOwner.getSharedPreferences(TITLES, 0).getAll();
	        ImageAdapter.this.mNumLoaded = 0;
	        int movieCount = movieTitles.size();
	        ImageAdapter.this.movies = new MovieData[movieCount];
	        // Initialise app with cached data
	        for (Map.Entry<String, ?> movieData : movieTitles.entrySet()) {
	        	ImageAdapter.this.movies[ImageAdapter.this.mNumLoaded] = new MovieData(ImageAdapter.this.mOwner, movieData);
	        	ImageAdapter.this.mNumLoaded++;
	        }
			ImageAdapter.this.notifyDataSetChanged();
		}

		@Override
		protected void after(Context ctx, JSONArray jsonMovies) {
			// Save the IDs of the cached movies so their poster thumbnail image can be deleted later
			Map<String, ?> movieTitles = ImageAdapter.this.mOwner.getSharedPreferences(TITLES, 0).getAll();
			Set<Integer> losers = new HashSet<Integer>(movieTitles.size());
	        for (String id : movieTitles.keySet()) {
	        	losers.add(Integer.parseInt(id));
	        }
        	
			ImageAdapter.this.mNumLoaded = 0;
			ImageAdapter.this.movies = new MovieData[jsonMovies.length()];
			new ImgLoadTask(this.getCallingContext(), ImageAdapter.this.movies.length - 1, losers).execute(jsonMovies);
		}
	}
	
	private class MovieImageView extends ImageView {
		int movieID;

		MovieImageView(Context context, int movieID) {
			super(context);
			init(movieID);
		}

		/**
		 * @param id
		 */
		void init(int id) {
			this.movieID = id;
			OnClickListener showMovieInfo = new OnClickListener() {
				public void onClick(View v) {
					MovieImageView selection = (MovieImageView)v;
					Intent intentToWatch = new Intent(ImageAdapter.this.mOwner, Movie.class);
					intentToWatch.putExtra(Movie.MOVIE_ID, selection.movieID);
					ImageAdapter.this.mOwner.startActivity(intentToWatch);
				}
			};
			this.setOnClickListener(showMovieInfo);
		}
		
	}
	
	/**
	 * Construct a new ImageAdapter.
	 */
	public ImageAdapter(Movies owner) {
        this.mOwner = owner;
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
		return this.movies[position];
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return this.movies[position].getId();
	}

	/* (non-Javadoc)
	 * Create a new ImageView for each item referenced by the Adapter
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		MovieImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialise some attributes
            imageView = new MovieImageView(this.mOwner, this.movies[position].getId());
            imageView.setLayoutParams(new GridView.LayoutParams(95, 140));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (MovieImageView) convertView;
            imageView.init(this.movies[position].getId());
        }
		imageView.setImageBitmap(this.movies[position].getThumbnail());
        return imageView;
	}

}
