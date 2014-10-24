package com.door43.translationstudio.util;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

import com.door43.translationstudio.MainApplication;

/**
 * Custom activity class to provide some of the heavy lifting.
 * Every activity within the app should extend this base activity.
 */
public class TranslatorBaseActivity extends ActionBarActivity {

    protected void onResume() {
        super.onResume();
        MainContext.getEventBus().register(this);
        // set the current activity so that core classes can access the ui when nessesary.
        app().setCurrentActivity(this);
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    /**
     * Returns the MainApplication context
     * @return
     */
    public MainApplication app() {
        return ((MainApplication) this.getApplication());
    }

    /**
     * Removes references to self to avoid memory leaks
     */
    private void clearReferences() {
        MainContext.getEventBus().unregister(this);
        Activity currActivity = app().getCurrentActivity();
        if(currActivity != null && currActivity.equals(this)) {
            app().setCurrentActivity(null);
        }
    }
}