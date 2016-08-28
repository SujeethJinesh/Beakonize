package org.bluetooth.blebeacon;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by lab on 7/15/2016.
 */
public class Database extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
