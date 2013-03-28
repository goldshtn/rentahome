package net.sashag.rentahome;

import java.io.Serializable;

import android.location.Location;

public class Apartment implements Serializable {
	
	private static final long serialVersionUID = 1912118880934258979L;

	private int id;	
	private String address;
	private boolean published;
	private int bedrooms;
	private double latitude;
	private double longitude;
	private String username;

	@Override
	public String toString() {
		return address;
	}
	
	public int getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public int getBedrooms() {
		return bedrooms;
	}

	public void setBedrooms(int bedrooms) {
		this.bedrooms = bedrooms;
	}
	
	public Location getLocation() {
		Location location = new Location("gps");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}

	public String getUserName() {
		return username;
	}
}
