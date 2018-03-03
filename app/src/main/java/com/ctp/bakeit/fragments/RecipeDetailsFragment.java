package com.ctp.bakeit.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.adapters.RecipeStepsAdapter;
import com.ctp.bakeit.provider.BakeItContract;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/25/18.
 */

public class RecipeDetailsFragment extends Fragment
        implements RecipeStepsAdapter.RecipeStepsAdapterCallback{

    private static final String LOG_TAG = RecipeDetailsFragment.class.getSimpleName();



    private int clickedPosition;

    private RecipeDetailsFragmentCallback mCallback;

    public interface RecipeDetailsFragmentCallback{
        void onRecipeStepClicked(int stepNumber, int count);
    }


    public RecipeDetailsFragment() {

    }

    @BindView(R.id.recipe_details_steps_list)  RecyclerView recipeStepRecyclerView;
    @BindView(R.id.recipe_details_ingredient_item) TextView ingredientItemView;
    @BindView(R.id.recipe_details_scroll_view)
    ScrollView scroller;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    private RecipeStepsAdapter recipeStepsAdapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recipe_details,container,false);
        ButterKnife.bind(this,rootView);


        LinearLayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recipeStepRecyclerView.setLayoutManager(manager);
        recipeStepRecyclerView.setHasFixedSize(true);
        recipeStepRecyclerView.setVerticalScrollBarEnabled(false);


        return rootView;


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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void setStepsData(Cursor data, int clickedPosition, final int scrollX,final int scrollY){
        if(data == null)
            return;
        this.clickedPosition = clickedPosition;
        Log.d(LOG_TAG,"Clicked position is "+clickedPosition);
        if(recipeStepsAdapter==null) {
            recipeStepsAdapter = new RecipeStepsAdapter(data, this,isTablet,clickedPosition);
            recipeStepRecyclerView.setAdapter(recipeStepsAdapter);
        }
        else {
            recipeStepsAdapter.swapCursor(data);
        }

        if(scrollX!=-1 && scrollY !=-1) {

            Log.d(LOG_TAG, " Fragment X offset = "+scrollX +" | Y offset is "+scrollY);
            scroller.post(new Runnable() {
                @Override
                public void run() {
                    scroller.scrollTo(scrollX,scrollY);
                }
            });

        }

    }

    @Override
    public void onRecipeStepClicked(int stepId, int count) {
        clickedPosition = stepId;
        mCallback.onRecipeStepClicked(stepId,count);
    }


    public void setIngredientsData(Cursor data){

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

    public int getClickedPosition() {
        return clickedPosition;
    }

    public int getScrollYPosition(){
        return scroller.getScrollY();
    }

    public int getScrollXPosition(){
        return scroller.getScrollX();
    }


}
