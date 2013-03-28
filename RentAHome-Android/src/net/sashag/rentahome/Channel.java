package net.sashag.rentahome;

@SuppressWarnings("unused")
public class Channel {

	private int id;
	private String uri;
	private String type;
	
	public Channel(String registrationId) {
		this.uri = registrationId;
		this.type = "Android";
	}
	
}
