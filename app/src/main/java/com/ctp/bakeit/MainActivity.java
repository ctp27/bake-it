package com.ctp.bakeit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ctp.bakeit.adapters.RecipeAdapter;
import com.ctp.bakeit.models.Recipe;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItUtils;
import com.ctp.bakeit.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        RecipeAdapter.RecipeAdapterCallback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int RECIPE_CURSOR_LOADER_ID = 211;
    private RecipeAdapter mRecipeAdapter;

    @BindView(R.id.recipes_recycler_view)  RecyclerView recipeRecyclerView;



    @BindBool(R.bool.isTablet) boolean isTablet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(isTablet){
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
            recipeRecyclerView.setLayoutManager(gridLayoutManager);
        }
        else {
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recipeRecyclerView.setLayoutManager(layoutManager);
        }
//        getContentResolver().delete(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,null,null);
//        getContentResolver().delete(BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,null,null);
//        getContentResolver().delete(BakeItContract.StepEntry.STEP_CONTENT_URI,null,null);

//        new DownloadJsonTask().execute(NetworkUtils.buildJsonRecipeUrl());
        getSupportLoaderManager().restartLoader(RECIPE_CURSOR_LOADER_ID,null, this);

    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case RECIPE_CURSOR_LOADER_ID:
                return new CursorLoader(this, BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,
                        null,null,null, BakeItContract.RecipeEntry.COLUMN_RECIPE_ID+" ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if(data==null)
            return;
        if(mRecipeAdapter==null) {
            mRecipeAdapter = new RecipeAdapter(data, this);
            recipeRecyclerView.setAdapter(mRecipeAdapter);
        }
        else {
            mRecipeAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        if(mRecipeAdapter!=null) {
            mRecipeAdapter.swapCursor(null);
        }
    }



    @Override
    public void onRecipeClicked(int recipeId) {
        Uri uri = BakeItContract.RecipeEntry.getRecipeContentUriForId(recipeId);
        Intent intent = new Intent(this,RecipeDetailsActivity.class);
        intent.setData(uri);
        startActivity(intent);

    }

    private class DownloadJsonTask extends AsyncTask<URL,Void,String>{

        @Override
        protected void onPostExecute(String s) {

            List<Recipe> recipes = null;

            try {
                 recipes = BakeItUtils.getRecipeeListFromJson(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(recipes == null){
                Log.d(LOG_TAG,"recipees is null");
                return;
            }


            for(Recipe recipe: recipes){

                ContentValues recipeEntry = BakeItUtils.populateRecipeContentValue(recipe);
                Log.d(LOG_TAG,"recipe name is "+recipe.getName());
                Uri returnUri = getContentResolver().insert(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,recipeEntry);
                String id = returnUri.getLastPathSegment();
                ContentValues[] ingredientValues = BakeItUtils.populateIngredientContentValuesArray(
                            recipe.getIngredients(),Integer.parseInt(id));

                ContentValues[] stepValues = BakeItUtils.populateStepContentValuesArray(
                        recipe.getSteps(),Integer.parseInt(id));

                getContentResolver()
                        .bulkInsert(BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,ingredientValues);

                getContentResolver()
                        .bulkInsert(BakeItContract.StepEntry.STEP_CONTENT_URI,stepValues);

            }
        }

        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = NetworkUtils.getResponseFromHttpUrl(urls[0]);
            }
            catch (IOException e){

            }
            return response;
        }
    }
}
