package ca.cineti.android.test;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;
import ca.cineti.android.MovieData;

public class MovieDataTest extends AndroidTestCase {

	public MovieDataTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMovieDataContextJSONObject() {
		Context ctx = getContext();
		JSONObject json = new JSONObject();
		
		MovieData target = new MovieData(ctx, json);
		assertEquals(0, target.getId());
	}

}
