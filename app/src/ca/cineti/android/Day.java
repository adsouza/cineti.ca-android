package ca.cineti.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * Represents a day. Duh.
 */
public enum Day {
	mon("Monday"), 
	tue("Tuesday"), 
	wed("Wednesday"), 
	thu("Thursday"), 
	fri("Friday"),
	sat("Saturday"),
	sun("Sunday");
	
	static Day[] week = { mon, tue, wed, thu, fri, sat, sun };
	
	private String fullName;
	
	private Day(String longForm) {
		this.fullName = longForm;
	}
	
	@Override
	public String toString() {
		return this.fullName;
	}
	
	public Day next() {
		int newIdx = (this.ordinal() + 1) % week.length;
		return week[newIdx];
	}
	
	static Day parseString(String ascii) throws Exception {
		for (Day d : week) {
			if (d.fullName.equals(ascii) || d.name().equalsIgnoreCase(ascii)) {
				return d;
			}
		}
		throw new Exception("Unrecognized day name: " + ascii);
	}
	
	public static Day today() {
		try {
			return parseString(new SimpleDateFormat("E").format(new Date()));
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("cineti", "Bizzarely unable to figure out which day of the week it is!", e);
			return null;
		}
	}
}
