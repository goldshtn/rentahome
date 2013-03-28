package net.sashag.rentahome;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ApartmentAdapter extends ArrayAdapter<Apartment> {

	private Context context;
	
	public ApartmentAdapter(Context context, List<Apartment> apartments) {
		super(context, android.R.layout.simple_list_item_1, apartments);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Apartment apartment = getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.apartment_row, null);
		}
		TextView txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
		TextView txtSecondary = (TextView) convertView.findViewById(R.id.txtSecondary);
		TextView txtBedrooms = (TextView) convertView.findViewById(R.id.txtBedrooms);
		txtAddress.setText(apartment.getAddress());
		txtSecondary.setText(String.format("added by %s", apartment.getUserName()));
		txtBedrooms.setText(Integer.toString(apartment.getBedrooms()));
		return convertView;
	}
	
}
