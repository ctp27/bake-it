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
import com.ctp.bakeit.adapters.IngredientListAdapter;
import com.ctp.bakeit.adapters.RecipeStepsAdapter;
import com.ctp.bakeit.utils.BakeItPreferences;
import com.ctp.bakeit.widget.IngredientWidgetService;

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
    private String recipeId;

    public interface RecipeDetailsFragmentCallback{
        void onRecipeStepClicked(int stepNumber, int count);
        void onAddToWidgetButtonPressed(View v);
    }


    public RecipeDetailsFragment() {

    }

    @BindView(R.id.recipe_details_steps_list)  RecyclerView recipeStepRecyclerView;
    @BindView(R.id.recipe_details_ingredient_list) RecyclerView ingredientsRecyclerView;
    @BindView(R.id.recipe_details_scroll_view)
    ScrollView scroller;

    @BindView(R.id.recipe_details_add_to_widget_btn)
    TextView addToWidgetTextView;

    @BindView(R.id.recipe_details_unpin_widget_btn)
    TextView removeWidgetButton;

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

        LinearLayoutManager manager2 = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        ingredientsRecyclerView.setLayoutManager(manager2);
        ingredientsRecyclerView.setHasFixedSize(true);
        recipeStepRecyclerView.setVerticalScrollBarEnabled(false);

        addToWidgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onAddToWidgetButtonPressed(v);
                displayUnpinWidgetButton();
            }
        });

        removeWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IngredientWidgetService.startServiceUnpinRecipe(getContext());
                displayPinWidgetButton();
            }
        });



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
        IngredientListAdapter adapter = new IngredientListAdapter(data);
        ingredientsRecyclerView.setAdapter(adapter);
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

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
        if(BakeItPreferences.isRecipeAddedToWidget(getContext(),recipeId)){
            displayUnpinWidgetButton();
        }
        else {
            displayPinWidgetButton();
        }

    }

    private void displayUnpinWidgetButton() {
        removeWidgetButton.setVisibility(View.VISIBLE);
        addToWidgetTextView.setVisibility(View.INVISIBLE);
    }

    private void displayPinWidgetButton() {
        addToWidgetTextView.setVisibility(View.VISIBLE);
        removeWidgetButton.setVisibility(View.INVISIBLE);
    }
}
