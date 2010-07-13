package ca.cineti.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Displays info about the screenings for a particular movie at a particular cinema.
 *
 */
public class ScreeningsAdapter extends ArrayAdapter<MovieData> {

	private int resource;
	private Cinema cinema;

	public ScreeningsAdapter(Context context, 
							int resource,  
							List<MovieData> items,
							Cinema cinema) {
		super(context, resource, items);
		this.resource = resource;
		this.cinema = cinema;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout screeningView;
		if (convertView == null) {  // if it's not recycled, initialise some attributes
			screeningView = new LinearLayout(this.getContext());
			LayoutInflater pump = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			pump.inflate(this.resource, screeningView, true);
		} else {
			screeningView = (LinearLayout)convertView;
		}
		MovieData data = this.getItem(position);
		TextView title = (TextView)screeningView.findViewById(R.id.movieTitle);
		title.setText(data.getTitle());
		TextView screenings = (TextView)screeningView.findViewById(R.id.screenings);
		screenings.setText(data.getScreenings(this.cinema));
		ImageView thumbnail = (ImageView)screeningView.findViewById(R.id.thumbnail);
		thumbnail.setImageBitmap(data.getThumbnail());
		return screeningView;
	}

}
