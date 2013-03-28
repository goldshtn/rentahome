package net.sashag.rentahome;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(GCMRegistrationManager.GCM_SENDER_ID);
	}
	
	@Override
	protected void onError(Context context, String error) {
		Log.w("GCMIntentService", "An error related to GCM occurred: " + error);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Intent activityIntent = new Intent(this, RentAHomeActivity.class);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.appicon)
				.setContentTitle("New apartment added!")
				.setContentText(String.format("New %s-bedroom apartment added at %s",
						intent.getStringExtra("bedrooms"), intent.getStringExtra("address")))
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		GCMRegistrationManager.setRegistrationId(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		GCMRegistrationManager.setRegistrationId(null);
	}
}
