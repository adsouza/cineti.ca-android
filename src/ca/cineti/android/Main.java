package ca.cineti.android;

import android.app.TabActivity;
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
        
        // Initialise a TabSpec for each tab and add it to the TabHost.
        spec = tabHost.newTabSpec("movies").setIndicator("Movies").setContent(R.id.txt1);
        tabHost.addTab(spec);
        spec = tabHost.newTabSpec("theatres").setIndicator("Theatres").setContent(R.id.txt2);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
}