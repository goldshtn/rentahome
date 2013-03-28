package net.sashag.rentahome;

public class GCMRegistrationManager {
	
	public static final String GCM_SENDER_ID = "489244549308";
	
	private static String registrationId;
	private static Runnable callback;
	
	public static void setCallback(Runnable callback) {
		GCMRegistrationManager.callback = callback;
	}

	public static String getRegistrationId() {
		return registrationId;
	}

	public static void setRegistrationId(String registrationId) {
		GCMRegistrationManager.registrationId = registrationId;
		if (callback != null) {
			callback.run();
		}
	}
}
