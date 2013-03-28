package net.sashag.rentahome;

import java.net.MalformedURLException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.QueryOrder;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;

@EActivity
@OptionsMenu(R.menu.activity_rent_ahome)
public class RentAHomeActivity extends Activity {

	private static final String MOBILESERVICE_URL = "https://rentahome.azure-mobile.net";
	private static final String MOBILESERVICE_APIKEY = "VvqfVLoQPInuOScAVsQuHIxhSrbGmI34";
	
	@ViewById
	protected ListView listApartments;
	
	private MobileServiceClient mobileService;
	private MobileServiceTable<Apartment> apartmentTable;
	private List<Apartment> apartments;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_ahome);
    }
    
    private void setupGCM() {
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	String registrationId = GCMRegistrar.getRegistrationId(this);
		GCMRegistrationManager.setCallback(new Runnable() {
			public void run() {
				mobileService.getTable("channels", Channel.class).insert(
					new Channel(GCMRegistrationManager.getRegistrationId()),
					new TableOperationCallback<Channel>() {
						public void onCompleted(Channel item, Exception exception, ServiceFilterResponse response) {
							if (exception != null) {
								displayError(exception);
							}
						}
					});
			}
		});
    	if ("".equals(registrationId)) {
    		GCMRegistrar.register(this, GCMRegistrationManager.GCM_SENDER_ID);
    	} else {
    		GCMRegistrationManager.setRegistrationId(registrationId);
    	}
    }
    
    @AfterViews
    protected void init() {
        registerForContextMenu(listApartments);
    	try {
			mobileService = new MobileServiceClient(MOBILESERVICE_URL, MOBILESERVICE_APIKEY, this);
		} catch (MalformedURLException e) {
			displayError(e);
		}
    	setupGCM();
        apartmentTable = mobileService.getTable("apartment", Apartment.class);
        loadApartments();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	Apartment apartment = apartments.get(info.position);
    	getMenuInflater().inflate(R.menu.apatment_context_menu, menu);
    	menu.setHeaderTitle(apartment.getAddress());
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	Apartment apartment = apartments.get(info.position);
    	
    	switch (item.getItemId()) {
    	case R.id.menu_item_delete:
    		deleteApartment(apartment);
    		return true;
    	case R.id.menu_item_unpublish:
    		apartment.setPublished(false);
    		updateApartment(apartment);
    		return true;
    	}
    	return false;
    }
    
    @OptionsItem(R.id.menu_add_apartment)
    protected void menuAdd() {
    	
    	View innerLayout = getLayoutInflater().inflate(R.layout.add_new_apartment, null);
    	final EditText editAddress = (EditText) innerLayout.findViewById(R.id.editAddress);
    	final Spinner spinBedrooms = (Spinner) innerLayout.findViewById(R.id.spinBedrooms);
    	Integer[] rooms = { 1, 2, 3, 4, 5, 6, 7 };
    	ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, rooms);
    	spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinBedrooms.setAdapter(spinnerAdapter);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Add New Apartment");
    	builder.setView(innerLayout);
    	builder.setPositiveButton("Submit", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		        Apartment apartment = new Apartment();
		        apartment.setAddress(editAddress.getText().toString());
		        apartment.setBedrooms((Integer)spinBedrooms.getSelectedItem());
		        apartment.setPublished(true);
		    	insertApartment(apartment);
			}
		});
    	builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { }
		});
    	builder.create().show();
    }
    
    @OptionsItem(R.id.menu_refresh)
    protected void menuRefresh() {
    	loadApartments();
    }
    
    @OptionsItem(R.id.menu_map)
    protected void menuMap() {
    	ApartmentsMapActivity_.intent(this).apartments(apartments).start();
    }
    
    @OptionsItem(R.id.menu_login)
    protected void menuLogin() {
    	mobileService.login(MobileServiceAuthenticationProvider.Twitter, new UserAuthenticationCallback() {
			public void onCompleted(MobileServiceUser user, Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					displayError(exception);
				} else {
					setTitle("Welcome! User id: " + user.getUserId());
				}
			}
		});
    }
    
    protected void insertApartment(Apartment apartment) {
		apartmentTable.insert(apartment, new TableOperationCallback<Apartment>() {
			public void onCompleted(Apartment item, Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					displayError(exception);
				} else {
					loadApartments();
					Log.i("RentAHomeActivity", "Inserted apartment id: " + item.getId());						
				}
			}
		});
    }
    
    protected void deleteApartment(Apartment apartment) {
		apartmentTable.delete(apartment, new TableDeleteCallback() {
			public void onCompleted(Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					displayError(exception);
				} else {
					loadApartments();
				}
			}
		});
    }
    
    protected void updateApartment(Apartment apartment) {
		apartmentTable.update(apartment, new TableOperationCallback<Apartment>() {
			public void onCompleted(Apartment item, Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					displayError(exception);
				} else {
					loadApartments();
				}
			}
		});
    }
    
    protected void loadApartments() {    	
    	apartmentTable
    		.where()
    		.field("published").eq(true).and()
    		.field("bedrooms").gt(1)
    		.orderBy("bedrooms", QueryOrder.Descending)
    		.execute(new TableQueryCallback<Apartment>() {
				public void onCompleted(List<Apartment> items, int count, Exception exception, ServiceFilterResponse response) {
					if (exception != null) {
						displayError(exception);
					} else {
						apartments = items;
						displayResults(apartments);
					}
				}
    	});
    }
    
    @UiThread
    protected void displayResults(List<Apartment> apartments) {
    	ApartmentAdapter adapter = new ApartmentAdapter(this, apartments);
		listApartments.setAdapter(adapter);
    }
    
    @UiThread
    protected void displayError(Exception e) {
    	e.printStackTrace();
    	Toast.makeText(this, "Something went wrong: " + e.toString(), Toast.LENGTH_LONG).show();
    }
}
