package ca.cineti.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

/**
 *	This activity displays a scrollable list of movie poster thumbnails.
 */
public class Movies extends Activity {
	private GridView mGridView;
	private ImageAdapter mImgAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies);

        this.mGridView = (GridView) findViewById(R.id.gridview);
        this.mImgAdapter = new ImageAdapter(this);
		this.mGridView.setAdapter(this.mImgAdapter);
    }

	void refresh() {
		this.mGridView.setAdapter(this.mImgAdapter);
	}
}
