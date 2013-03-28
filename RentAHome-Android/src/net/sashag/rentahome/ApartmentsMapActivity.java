package net.sashag.rentahome;

import java.util.List;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.NoTitle;

@EActivity
@NoTitle
public class ApartmentsMapActivity extends MapActivity {

	@Extra
	protected List<Apartment> apartments;
	
	private MapView mapView;
	private ApartmentOverlay apartmentOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapView = new MapView(this, "0JbCWNTyTPTuadZqMA6QGaSuv1_-qjb4w64RDbQ");
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
		setContentView(mapView);
	}
	
	@AfterViews
	protected void initMap() {
		apartmentOverlay = new ApartmentOverlay(this, getResources().getDrawable(android.R.drawable.btn_star));
		for (Apartment apartment : apartments) {
			apartmentOverlay.addApartment(apartment);
		}
		mapView.getOverlays().add(apartmentOverlay);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
