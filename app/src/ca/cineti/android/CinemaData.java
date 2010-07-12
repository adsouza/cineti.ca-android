package ca.cineti.android;

public enum CinemaData {
	AMC("AMC Forum 22"), 
	Scotia("Banque Scotia (Paramount)"), 
	Beaubien("Cinema Beaubien"), 
	Parc("Cinema du Parc"), 
	Latin("Quartier Latin");
	
	static CinemaData[] array = { AMC, Scotia,  Beaubien, Parc, Latin };
	
	private String fullName;
	
	private CinemaData(String longForm) {
		this.fullName = longForm;
	}
	
	public String toString() {
		return this.fullName;
	}
	
	static CinemaData parseString(String ascii) throws Exception {
		for (CinemaData c : array) {
			if (c.fullName.equals(ascii)) {
				return c;
			}
		}
		throw new Exception("Unrecognized cinema name: " + ascii);
	}
}