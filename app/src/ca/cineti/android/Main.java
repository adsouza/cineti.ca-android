package ca.cineti.android;

import org.apache.http.HttpHost;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class Main extends TabActivity {
    static final String PKG_NAME = Main.class.getPackage().getName();
	static final HttpHost targetHost = new HttpHost("api.cineti.ca", 80, "http");

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        setProgressBarIndeterminateVisibility(true);
        
        TabHost tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the Movies tab.
        intent = new Intent().setClass(this, Movies.class);
        // Initialise a TabSpec for the Movies tab and add it to the TabHost.
        spec = tabHost.newTabSpec("movies").setIndicator(getString(R.string.movies)).setContent(intent);
        tabHost.addTab(spec);
        
        // Do the same things for the Theatres tab.
        intent = new Intent().setClass(this, Cinemas.class);
        spec = tabHost.newTabSpec("cinemas").setIndicator(getString(R.string.cinemas)).setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
}
