package net.sashag.rentahome;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ApartmentOverlay extends ItemizedOverlay<OverlayItem> {

	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private List<Apartment> apartments = new ArrayList<Apartment>();
	private Context context;

	public void addApartment(Apartment apartment) {
		Location location = apartment.getLocation();
		items.add(new OverlayItem(new GeoPoint(
				(int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6)),
				"Apartment",
				apartment.getAddress()));
		apartments.add(apartment);
		populate();
	}

	public void clearApartments() {
		items.clear();
		apartments.clear();
		populate();
	}

	public ApartmentOverlay(Context ctx, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		context = ctx;
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	protected boolean onTap(int index) {
		Apartment apartment = apartments.get(index);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Apartment");
		builder.setMessage("Address: " + apartment.getAddress() + "\n" + apartment.getBedrooms() + " bedrooms");
		builder.create().show();
		return super.onTap(index);
	}

	public List<Apartment> getApartments() {
		return apartments;
	}

}

