package ca.cineti.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 *	Displays a brief list of local movie theatres.
 */
public class Cinemas extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This should be a brief list of local movie theatres.");
        setContentView(textview);
    }
}
