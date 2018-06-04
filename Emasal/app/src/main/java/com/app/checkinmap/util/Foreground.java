package com.app.checkinmap.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** This class help us to handle when the application go to
 *  background or come to foreground
 */
public class Foreground implements Application.ActivityLifecycleCallbacks {

    /*Here we define the constant*/
    public static final String TAG= Foreground.class.getName();
    public static final long CHECK_DELAY = 500;

    /*This is the instance of the singleton*/
    private static Foreground instance;

    /*This is the flag  for know when the app
      is in the foreground*/
    private boolean foreground=false;

    /*This is the flag  for know when the app
      is paused*/
    private boolean paused=true;

    /*Empty constructor class*/
    private Foreground(){}

    /*List for add all the listener*/
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    /*Interface to notify the app state*/
    public interface Listener {
        void onBecameForeground();
        void onBecameBackground();
    }

    /* object to handle a post after delay*/
    private Handler handler = new Handler();

    /*post to make after delay*/
    private Runnable check;

    /*This method help us to initialize the singleton*/
    public static void init(Application app){
        if (instance == null){
            instance = new Foreground();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    /*This method help us to get the instance*/
    public static Foreground get(){
        return instance;
    }


    /*This method help us to add the listener*/
    public void addListener(Listener listener){
        listeners.add(listener);
    }

    /*This method help us to remove the listener*/
    public void removeListener(Listener listener){
        listeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground){
            Log.i(TAG, "went foreground");
            for (Listener l : listeners) {
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    Log.e(TAG, "Listener threw exception!", exc);
                }
            }
        } else {
            Log.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.postDelayed(check = new Runnable(){
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.i(TAG, "went background");
                    for (Listener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.e(TAG, "Listener threw exception!", exc);
                        }
                    }
                } else {
                    Log.i(TAG, "still foreground");
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}