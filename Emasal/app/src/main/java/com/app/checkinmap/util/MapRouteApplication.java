package com.app.checkinmap.util;

import android.app.Application;

import com.app.checkinmap.ui.activity.SplashScreenActivity;
import com.crashlytics.android.Crashlytics;
import com.salesforce.androidsdk.smartsync.app.SmartSyncSDKManager;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * This class help us to init
 * the ORM in the application
 */

public class MapRouteApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        SmartSyncSDKManager.initNative(getApplicationContext(), new NativeKeyImpl(), SplashScreenActivity.class);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
            .Builder()
            .deleteRealmIfMigrationNeeded()
            .build();
        Realm.setDefaultConfiguration(config);

        //Here we register the foreground class
        Foreground.init(this);
    }

}
