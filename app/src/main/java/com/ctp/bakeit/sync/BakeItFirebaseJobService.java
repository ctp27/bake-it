package com.ctp.bakeit.sync;


import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by clinton on 2/27/18.
 */

public class BakeItFirebaseJobService extends JobService {


    private AsyncTask<Void,Void,Void> syncTask;


    @Override
    public boolean onStartJob(final JobParameters job) {

        syncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                BakeSyncTask.syncRecipes(getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job,false);
            }
        };

        syncTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        if(syncTask!=null){
            syncTask.cancel(true);
        }
        return true;
    }
}
