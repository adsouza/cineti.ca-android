package ca.cineti.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

/**
 *	This activity displays a scrollable list of movie poster thumbnails.
 */
public class Movies extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
    }
}
