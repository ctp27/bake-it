package com.ctp.bakeit.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by clinton on 2/27/18.
 */

public class BakeItSyncIntentService extends IntentService {

    public BakeItSyncIntentService() {
        super("bake-it-sync");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        BakeSyncTask.syncRecipes(this);
    }
}
