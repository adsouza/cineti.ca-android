package ca.cineti.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 *	This activity displays a scrollable list of movie poster thumbnails.
 */
public class Movies extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This should be a scrollable list of movie poster thumbnails.");
        setContentView(textview);
    }
}
