package com.ctp.bakeit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class StepDetailActivity extends AppCompatActivity {

    public static final String INTENT_RECIPE_ID_EXTRA="recipe-extra-key";
    private String recipeId;
    private Uri queryUri;
    private String stepId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);
        getIntentData();
        




    }


    private void getIntentData(){
        Intent recievedIntent = getIntent();
        if(recievedIntent!=null) {
            queryUri = recievedIntent.getData();
            if (recievedIntent.hasExtra(INTENT_RECIPE_ID_EXTRA)) {
                recipeId = recievedIntent.getStringExtra(INTENT_RECIPE_ID_EXTRA);
            }
        }
    }
}
