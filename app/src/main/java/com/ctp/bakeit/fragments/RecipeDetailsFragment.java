package com.ctp.bakeit.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.StepDetailActivity;
import com.ctp.bakeit.adapters.RecipeStepsAdapter;
import com.ctp.bakeit.provider.BakeItContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/25/18.
 */

public class RecipeDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                    RecipeStepsAdapter.RecipeStepsAdapterCallback{

    private static final String LOG_TAG = RecipeDetailsFragment.class.getSimpleName();
    private static final int CURSOR_LOADER_RECIPE_KEY = 101;
    private static final int CURSOR_LOADER_STEPS_KEY = 201;
    private static final int CURSOR_LOADER_INGREDIENTS_KEY = 301;
    private static final String BUNDLE_QUERY_KEY = "recipe_id_key";

    private Uri queryUri;
    private String recipeId;
    private boolean isRecyclerViewFocused;

    private RecipeDetailsFragmentCallback mCallback;

    public interface RecipeDetailsFragmentCallback{
        void onFragmentCreated(String name);
    }


    public RecipeDetailsFragment() {

    }

    @BindView(R.id.recipe_details_steps_list)  RecyclerView recipeStepRecyclerView;
    @BindView(R.id.recipe_details_ingredient_item) TextView ingredientItemView;

    private RecipeStepsAdapter recipeStepsAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recipe_details,container,false);
        ButterKnife.bind(this,rootView);
        if(savedInstanceState!=null){
            queryUri = Uri.parse(savedInstanceState.getString(BUNDLE_QUERY_KEY));
        }

        recipeId = queryUri.getLastPathSegment();

        LinearLayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recipeStepRecyclerView.setLayoutManager(manager);
        recipeStepRecyclerView.setHasFixedSize(true);
        recipeStepRecyclerView.setVerticalScrollBarEnabled(false);
//        recipeStepRecyclerView.setNestedScrollingEnabled(false);

        getLoaderManager().restartLoader(CURSOR_LOADER_RECIPE_KEY,null,this);
        getLoaderManager().restartLoader(CURSOR_LOADER_INGREDIENTS_KEY,null,this);
        getLoaderManager().restartLoader(CURSOR_LOADER_STEPS_KEY,null,this);

        return rootView;


    }


    public void setQueryUri(Uri queryUri) {
        this.queryUri = queryUri;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (RecipeDetailsFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnImageClickListener");
        }
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
        recipeStepsAdapter.swapCursor(null);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_QUERY_KEY,queryUri.toString());
    }



    private void setRecipeData(Cursor data){
        if(data==null) {
            return;
        }

        data.moveToFirst();
        String title = data.getString(data.getColumnIndex(BakeItContract.RecipeEntry.COLUMN_NAME));
        mCallback.onFragmentCreated(title);


    }

    private void setStepsData(Cursor data){
        if(data == null)
            return;
        if(recipeStepsAdapter==null) {
            recipeStepsAdapter = new RecipeStepsAdapter(data, this);
            recipeStepRecyclerView.setAdapter(recipeStepsAdapter);
        }
        else {
            recipeStepsAdapter.swapCursor(data);
        }

    }

    @Override
    public void onRecipeStepClicked(int stepId) {
        Uri uri = BakeItContract.StepEntry.getStepContentUriForId(stepId);
        Intent intent = new Intent(getContext(), StepDetailActivity.class);
        intent.setData(uri);
        intent.putExtra(StepDetailActivity.INTENT_RECIPE_ID_EXTRA,recipeId);
        Log.d(LOG_TAG,"Step Id is "+stepId);
        startActivity(intent);
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
            String finalData = "\u2022 \t"+quantity+measure +" - "+ingredName+"\n\n";
            ingredientItemView.append(finalData);
        }
    }
}
