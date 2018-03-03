package com.ctp.bakeit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.ctp.bakeit.fragments.StepDetailFragment;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class StepDetailActivity extends AppCompatActivity
            implements StepDetailFragment.StepDetailFragmentCallback{

    public static final String INTENT_RECIPE_NAME_EXTRA ="recipe-extra-key";
    public static final String INTENT_RECIPE_ID_EXTRA = "recipe0-id-extra";
    public static final String INTENT_RECIPE_STEP_NUMBER = "step_number";
    public static final String INTENT_RECIPE_STEP_COUNT = "blahblah";

    private String recipeName;
    private int stepNumber;
    private String recipeID;
    private int stepCount;

    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        enableFullScreenIfLandscape();
        setContentView(R.layout.activity_step_detail);
        getIntentData();


        if(savedInstanceState == null){
            StepDetailFragment fragment = new StepDetailFragment();
            fragment.setRecipeId(recipeID);
            fragment.setStepNumber(stepNumber);
            fragment.setCount(stepCount);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.step_detail_frame,fragment)
                    .commit();
        }
        else {
            restoreSavedInstanceState(savedInstanceState);
        }

    }


    private void getIntentData(){
        Intent recievedIntent = getIntent();
        if(recievedIntent!=null) {
            if (recievedIntent.hasExtra(INTENT_RECIPE_NAME_EXTRA)) {
                recipeName = recievedIntent.getStringExtra(INTENT_RECIPE_NAME_EXTRA);
                if(getSupportActionBar()!=null) {
                    getSupportActionBar().setTitle(recipeName);
                }
            }
            if(recievedIntent.hasExtra(INTENT_RECIPE_STEP_NUMBER)){
                stepNumber = recievedIntent.getIntExtra(INTENT_RECIPE_STEP_NUMBER,0);
            }
            if(recievedIntent.hasExtra(INTENT_RECIPE_ID_EXTRA)){
                recipeID = recievedIntent.getStringExtra(INTENT_RECIPE_ID_EXTRA);
            }
            if(recievedIntent.hasExtra(INTENT_RECIPE_STEP_COUNT)){
                stepCount = recievedIntent.getIntExtra(INTENT_RECIPE_STEP_COUNT,0);
            }
        }
    }

    private void enableFullScreenIfLandscape(){
        if(isLandscape){
            if(getSupportActionBar()!=null) {
                getSupportActionBar().hide();
            }
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INTENT_RECIPE_STEP_COUNT,stepCount);
    }

    private void restoreSavedInstanceState(Bundle bundle){
        stepCount = bundle.getInt(INTENT_RECIPE_STEP_COUNT);
    }


    @Override
    public void onNextBtnClicked(int stepId) {

    }

    @Override
    public void onPrevBtnClicked(int stepId) {

    }


}
