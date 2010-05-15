package ca.cineti.android;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Main extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the Movies tab.
        intent = new Intent().setClass(this, Movies.class);
        // Initialise a TabSpec for the Movies tab and add it to the TabHost.
        spec = tabHost.newTabSpec("movies").setIndicator(getResources().getText(R.string.movies)).setContent(intent);
        tabHost.addTab(spec);
        
        // Do the same things for the Theatres tab.
        intent = new Intent().setClass(this, Theatres.class);
        spec = tabHost.newTabSpec("cinemas").setIndicator(getResources().getText(R.string.cinemas)).setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
}
