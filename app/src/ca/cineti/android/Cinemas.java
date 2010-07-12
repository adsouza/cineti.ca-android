package ca.cineti.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 *	Displays a brief list of local movie theatres.
 */
public class Cinemas extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinemas);
        ListView cinemas = (ListView)findViewById(R.id.cinemas);
        //cinemas.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
        cinemas.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		TextView tv = (TextView)v;
        		String chars = tv.getText().toString();
        		try {
					CinemaData cd = CinemaData.parseString(chars);
					String url = "http://api.cineti.ca/theater/" + cd.name() + ".json";
					Log.d("cinetica", url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}});
	}
}
