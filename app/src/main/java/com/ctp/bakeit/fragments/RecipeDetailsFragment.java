package com.ctp.bakeit.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.provider.BakeItContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/25/18.
 */

public class RecipeDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_RECIPE_KEY = 101;
    private static final int CURSOR_LOADER_STEPS_KEY = 201;
    private static final int CURSOR_LOADER_INGREDIENTS_KEY = 301;

    private Uri queryUri;
    private String recipeId;

    public RecipeDetailsFragment() {

    }

    @BindView(R.id.recipe_details_list)  RecyclerView mRecipeDetailsView;
    @BindView(R.id.test_recipe_details_view) TextView testView;
    @BindView(R.id.test_recipe_details_view2) TextView testView2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recipe_details,container,false);
        ButterKnife.bind(this,rootView);
        recipeId = queryUri.getLastPathSegment();

        getLoaderManager().restartLoader(CURSOR_LOADER_RECIPE_KEY,null,this);
        getLoaderManager().restartLoader(CURSOR_LOADER_INGREDIENTS_KEY,null,this);
        getLoaderManager().restartLoader(CURSOR_LOADER_STEPS_KEY,null,this);
        return rootView;
    }


    public void setQueryUri(Uri queryUri) {
        this.queryUri = queryUri;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case CURSOR_LOADER_RECIPE_KEY:
                return new CursorLoader(getContext(),queryUri,
                        null,null,null,null);
            case CURSOR_LOADER_INGREDIENTS_KEY:
                return new CursorLoader(getContext(), BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,
                        null, BakeItContract.IngredientEntry.COLUMN_RECIPE_ID +"=?",
                        new String[]{recipeId}, BakeItContract.IngredientEntry._ID+" ASC");
            case CURSOR_LOADER_STEPS_KEY:
                return new CursorLoader(getContext(), BakeItContract.StepEntry.STEP_CONTENT_URI,
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
                setStepsData(data);
                break;
            case CURSOR_LOADER_INGREDIENTS_KEY:
                setIngredientsData(data);
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setRecipeData(Cursor data){
        if(data==null) {
            return;
        }

        data.moveToFirst();
        String title = data.getString(data.getColumnIndex(BakeItContract.RecipeEntry.COLUMN_NAME));
//        getActivity().getActionBar().setTitle(title);

//        TODO : get number of ingredients and number of servings

    }

    private void setStepsData(Cursor data){
        if(data == null)
            return;

        while(data.moveToNext()){
            String shortDescription = data.getString(data.getColumnIndex(BakeItContract.StepEntry.COLUMN_SHORT_DESC));
            testView2.append(shortDescription+"\n\n");
        }


    }

    private void setIngredientsData(Cursor data){

        if(data==null)
            return;

        while(data.moveToNext()){
            float quantity = data.getFloat(
                    data.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_QUANTITY));
            String measure = data.getString(
                    data.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_MEASURE));
            String ingredName = data.getString(
                    data.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_NAME));
            String finalData = ingredName +" - "+quantity+measure+"\n";
            testView.append(finalData);
        }
    }
}
