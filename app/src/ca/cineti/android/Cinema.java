package ca.cineti.android;

public enum Cinema {
	AMC("AMC Forum 22"), 
	Scotia("Banque Scotia (Paramount)"), 
	Beaubien("Cinema Beaubien"), 
	Parc("Cinema du Parc"), 
	Latin("Quartier Latin");
	
	static Cinema[] array = { AMC, Scotia,  Beaubien, Parc, Latin };
	
	private String fullName;
	
	private Cinema(String longForm) {
		this.fullName = longForm;
	}
	
	@Override
	public String toString() {
		return this.fullName;
	}
	
	static Cinema parseString(String ascii) throws Exception {
		for (Cinema c : array) {
			if (c.fullName.equals(ascii) || c.name().equals(ascii)) {
				return c;
			}
		}
		throw new Exception("Unrecognized cinema name: " + ascii);
	}
}