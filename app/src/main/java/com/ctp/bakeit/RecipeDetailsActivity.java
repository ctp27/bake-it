package com.ctp.bakeit;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ctp.bakeit.fragments.RecipeDetailsFragment;
import com.ctp.bakeit.fragments.StepDetailFragment;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.widget.IngredientWidgetService;

import butterknife.BindBool;
import butterknife.ButterKnife;

public class RecipeDetailsActivity extends AppCompatActivity
                implements RecipeDetailsFragment.RecipeDetailsFragmentCallback,
                LoaderManager.LoaderCallbacks<Cursor>,
                StepDetailFragment.StepDetailFragmentCallback {

    private static final int CURSOR_LOADER_RECIPE_KEY = 101;
    private static final int CURSOR_LOADER_STEPS_KEY = 201;
    private static final int CURSOR_LOADER_INGREDIENTS_KEY = 301;
    private static final String BUNDLE_CLICKED_POSITION_KEY = "bundle-position-key";
    private static final String BUNDLE_SCROLL_X_KEY = "bundle-scroller-off";
    private static final String LOG_TAG = RecipeDetailsActivity.class.getSimpleName();
    private static final String BUNDLE_SCROLL_Y_KEY = "bundle-scroller-y" ;

    private String recipeId;
    private String recipeTitle;
    private int clickedPosition=0;
    private int scrollXOffset =-1;

    private boolean isFirstTime=true;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    private RecipeDetailsFragment recipeDetailsFragment;
    private StepDetailFragment stepDetailFragment;

    private Uri uri;
    private int scrollYOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        ButterKnife.bind(this);

        if(savedInstanceState!=null){
            clickedPosition = savedInstanceState.getInt(BUNDLE_CLICKED_POSITION_KEY);
            scrollXOffset = savedInstanceState.getInt(BUNDLE_SCROLL_X_KEY);
            scrollYOffset = savedInstanceState.getInt(BUNDLE_SCROLL_Y_KEY);
            isFirstTime = false;
        }
            Intent recievedIntent = getIntent();
            if (recievedIntent != null) {
                uri = recievedIntent.getData();
                recipeId = uri.getLastPathSegment();
            }

            recipeDetailsFragment = new RecipeDetailsFragment();

            /* Set the queryUri for the fragment*/

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.recipe_details_fragment, recipeDetailsFragment)
                    .commit();


        getSupportLoaderManager().restartLoader(CURSOR_LOADER_RECIPE_KEY,null,this);
        getSupportLoaderManager().restartLoader(CURSOR_LOADER_INGREDIENTS_KEY,null,this);
        getSupportLoaderManager().restartLoader(CURSOR_LOADER_STEPS_KEY,null,this);
    }



    @Override
    public void onNextBtnClicked(int stepNumber) {
//        TODO: highlightCurrentPosition
    }

    @Override
    public void onPrevBtnClicked(int stepNumber) {
//        TODO : highlight current adapter position
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case CURSOR_LOADER_RECIPE_KEY:
                return new CursorLoader(this,uri,
                        null,null,null,null);
            case CURSOR_LOADER_INGREDIENTS_KEY:
                return new CursorLoader(this, BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,
                        null, BakeItContract.IngredientEntry.COLUMN_RECIPE_ID +"=?",
                        new String[]{recipeId}, BakeItContract.IngredientEntry._ID+" ASC");
            case CURSOR_LOADER_STEPS_KEY:
                return new CursorLoader(this, BakeItContract.StepEntry.STEP_CONTENT_URI,
                        null, BakeItContract.StepEntry.COLUMN_RECIPE_ID+"=?",
                        new String[]{recipeId}, BakeItContract.StepEntry._ID +" ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        switch (id){
            case CURSOR_LOADER_RECIPE_KEY:
                setRecipeData(data);
                break;
            case CURSOR_LOADER_STEPS_KEY:
                recipeDetailsFragment.setStepsData(data,clickedPosition, scrollXOffset,scrollYOffset);
                if(isTablet && isFirstTime){
                    StepDetailFragment stepDetailFragment = new StepDetailFragment();
                    stepDetailFragment.setCount(data.getCount());
                    stepDetailFragment.setStepNumber(0);
                    stepDetailFragment.setRecipeId(recipeId);

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.step_detail_frame,stepDetailFragment)
                            .commitAllowingStateLoss();
                }
                break;
            case CURSOR_LOADER_INGREDIENTS_KEY:
                recipeDetailsFragment.setIngredientsData(data);
                recipeDetailsFragment.setRecipeId(recipeId);
                break;

        }
    }


    private void setRecipeData(Cursor data){
        if(data==null) {
            return;
        }

        data.moveToFirst();
        recipeTitle = data.getString(data.getColumnIndex(BakeItContract.RecipeEntry.COLUMN_NAME));
        getSupportActionBar().setTitle(recipeTitle);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recipeDetailsFragment.setStepsData(null,0, scrollXOffset,scrollYOffset);
    }


    @Override
    public void onRecipeStepClicked(int stepNumber, int count) {
        if(isTablet){
//           TODO: Update Step Detail Activity
            StepDetailFragment stepDetailFragment = new StepDetailFragment();
            stepDetailFragment.setCount(count);
            stepDetailFragment.setStepNumber(stepNumber);
            stepDetailFragment.setRecipeId(recipeId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_detail_frame,stepDetailFragment)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, StepDetailActivity.class);
//        intent.putExtra(StepDetailActivity.)
            intent.putExtra(StepDetailActivity.INTENT_RECIPE_NAME_EXTRA, recipeTitle);
            intent.putExtra(StepDetailActivity.INTENT_RECIPE_ID_EXTRA, recipeId);
            intent.putExtra(StepDetailActivity.INTENT_RECIPE_STEP_NUMBER, stepNumber);
            intent.putExtra(StepDetailActivity.INTENT_RECIPE_STEP_COUNT, count);
            Log.d(LOG_TAG, "Step Id is " + stepNumber);
            startActivity(intent);
        }
    }


    @Override
    public void onAddToWidgetButtonPressed(View v) {
        IngredientWidgetService.startServiceSetWidget(this,recipeId,recipeTitle);
        TextView textView = (TextView) v;
        textView.setText("Added");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        clickedPosition =  recipeDetailsFragment.getClickedPosition();
        scrollXOffset = recipeDetailsFragment.getScrollXPosition();
        scrollYOffset = recipeDetailsFragment.getScrollYPosition();
        Log.d(LOG_TAG, "X offset = "+scrollXOffset +" | Y offset is "+scrollYOffset);
        outState.putInt(BUNDLE_CLICKED_POSITION_KEY,clickedPosition);
        outState.putInt(BUNDLE_SCROLL_X_KEY, scrollXOffset);
        outState.putInt(BUNDLE_SCROLL_Y_KEY,scrollYOffset);
    }
}
