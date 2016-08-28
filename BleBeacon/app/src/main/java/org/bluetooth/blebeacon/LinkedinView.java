package org.bluetooth.blebeacon;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by lab on 8/28/2016.
 */
public class LinkedinView extends Activity {

    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";
    public static final String EXTRAS_DEVICE_UUID    = "BLE_DEVICE_UUID";

    private WebView linkedinWebView;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    private String mDeviceID;

    private Firebase mRefBeaconList;
    private Firebase mRefRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linkedin_webview);
        linkedinWebView = (WebView) this.findViewById(R.id.linkedinWebView);

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = getIntent().getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";
        mDeviceID = getIntent().getStringExtra(EXTRAS_DEVICE_UUID);

        mRefRoot = new Firebase("https://beakonizer-7bd2f.firebaseio.com/");
        mRefBeaconList = new Firebase("https://beakonizer-7bd2f.firebaseio.com/BeaconList");

        mRefBeaconList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    String profile = null;
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        String UUID = postSnapshot.child("UUID").getValue(String.class);
                        profile = postSnapshot.child("Profile").getValue(String.class);
                        System.out.println(profile);
                    }
                    linkedinWebView.loadUrl(profile);
                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

}
