package com.ctp.bakeit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ctp.bakeit.fragments.RecipeDetailsFragment;
import com.ctp.bakeit.fragments.StepDetailFragment;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class RecipeDetailsActivity extends AppCompatActivity
                implements RecipeDetailsFragment.RecipeDetailsFragmentCallback,
                StepDetailFragment.StepDetailFragmentCallback{

    @BindBool(R.bool.isTablet)
    boolean isTablet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        ButterKnife.bind(this);

        if(savedInstanceState==null) {
            Intent recievedIntent = getIntent();
            Uri uri = null;
            if (recievedIntent != null) {
                uri = recievedIntent.getData();
            }

            RecipeDetailsFragment fragment = new RecipeDetailsFragment();

            /* Set the queryUri for the fragment*/
            fragment.setQueryUri(uri);


            getSupportFragmentManager().beginTransaction().
                    add(R.id.recipe_details_fragment, fragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentCreated(String name) {
        getSupportActionBar().setTitle(name);
    }


    @Override
    public void onRecipeDetailsLoaded(int stepCount, String recipeId, boolean isFirstTime) {

        if(isTablet && isFirstTime){
            StepDetailFragment stepDetailFragment = new StepDetailFragment();
            stepDetailFragment.setCount(stepCount);
            stepDetailFragment.setStepNumber(0);
            stepDetailFragment.setRecipeId(recipeId);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.step_detail_frame,stepDetailFragment)
                    .commitAllowingStateLoss();
        }

    }


    @Override
    public void onRecipeStepClicked(int stepNumber, int stepCount, String recipeId) {

            StepDetailFragment stepDetailFragment = new StepDetailFragment();
            stepDetailFragment.setCount(stepCount);
            stepDetailFragment.setStepNumber(stepNumber);
            stepDetailFragment.setRecipeId(recipeId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_detail_frame,stepDetailFragment)
                    .commit();

    }


    @Override
    public void onNextBtnClicked(int stepNumber) {
//        TODO: highlightCurrentPosition
    }

    @Override
    public void onPrevBtnClicked(int stepNumber) {
//        TODO : highlight current adapter position
    }




}
