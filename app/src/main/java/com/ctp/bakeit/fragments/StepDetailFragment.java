package com.ctp.bakeit.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.models.Step;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItUtils;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/26/18.
 */

public class StepDetailFragment extends Fragment
                implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = StepDetailFragment.class.getSimpleName();
    private static final int CURSOR_STEP_LOADER=101;

    private static final String BUNDLE_STEP_ID_KEY="key-step_id";
    private static final String BUNDLE_STEP_RECIPE_ID="key-recipe_id";
    private static final String BUNDLE_STEP_COUNT="key-count";





    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    @BindView(R.id.simpleExoPlayerView)
    SimpleExoPlayerView mSimpleExoPlayerView;


    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private TextView stepNumberView;
    private TextView shortDescriptionView;
    private TextView descriptionView;

//    private StepDetailFragmentCallback mCallback;


    private SimpleExoPlayer mExoPlayer;
    private int stepNumber;
    private String recipeId;
    private int count;


    public interface StepDetailFragmentCallback{
        void onNextBtnClicked(int stepId);
        void onPrevBtnClicked(int stepId);
        void setTitleBarName(String name);
    }

    public StepDetailFragment() {

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_step_detail,container,false);
        ButterKnife.bind(this,rootView);
        restoreVariablesIfSavedBundle(savedInstanceState);
        initializePortraidModeWidgets(rootView);

        getLoaderManager().restartLoader(CURSOR_STEP_LOADER,null,this);


        return rootView;
    }

    private void initializePortraidModeWidgets(View rootView) {

        if(!isLandscape){
            nextBtn = rootView.findViewById(R.id.step_detail_nxt_btn);
            prevBtn = rootView.findViewById(R.id.step_detail_prev_btn);
            stepNumberView = rootView.findViewById(R.id.step_detail_step_number_view);
            shortDescriptionView = rootView.findViewById(R.id.step_detail_short_description);
            descriptionView = rootView.findViewById(R.id.step_detail_description);

            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNextBtnClicked();

                }
            });

            prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPrevBtnClicked();

                }
            });

            if(stepNumber==count-1){
                nextBtn.setEnabled(false);
                nextBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_grey));
            }else {
                nextBtn.setEnabled(true);
            }

            if (stepNumber==0){
                prevBtn.setEnabled(false);
                prevBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_grey));
            }
            else
                prevBtn.setEnabled(true);

        }
        else {

        }
    }

    private void onNextBtnClicked() {
        int newStep=stepNumber+1;
        if(newStep>=count){
            newStep = stepNumber;
        }

//        mCallback.onNextBtnClicked(newStep);
        replaceFragment(newStep);
    }

    private void onPrevBtnClicked(){
        int newStep = stepNumber -1;
        if(newStep==-1){
            newStep = stepNumber;
        }
//        mCallback.onPrevBtnClicked(stepNumber);
        replaceFragment(newStep);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case CURSOR_STEP_LOADER:
                String selection = BakeItContract.StepEntry.COLUMN_STEP_NUMBER+"=? AND "
                        + BakeItContract.StepEntry.COLUMN_RECIPE_ID+"=?";
                String[] selectionArgs = {Integer.toString(stepNumber),recipeId};
                return new CursorLoader(getContext(), BakeItContract.StepEntry.STEP_CONTENT_URI,
                        null,selection,selectionArgs, BakeItContract.StepEntry.COLUMN_STEP_NUMBER+" ASC");

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data!=null){
            data.moveToFirst();

            Step thisStep = BakeItUtils.getStepFromContentValues(data);
            String description=null;
            if(thisStep.getId()!=0) {
                description = thisStep.getDescription().substring(3);
            }else {
                description = thisStep.getDescription();
            }

            if(!isLandscape){
                shortDescriptionView.setText(thisStep.getShortDescription());
                descriptionView.setText(description);
                stepNumberView.setText(getOutOfStepNumberString());
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        // This makes sure that the host activity has implemented the callback interface
//        // If not, it throws an exception
//        try {
//            mCallback = (StepDetailActivity) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement OnImageClickListener");
//        }
//    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"Saving instances");

        outState.putInt(BUNDLE_STEP_ID_KEY, stepNumber);
        outState.putString(BUNDLE_STEP_RECIPE_ID,recipeId);
        outState.putInt(BUNDLE_STEP_COUNT,count);
    }

    private void restoreVariablesIfSavedBundle(Bundle savedInstanceState){

        if(savedInstanceState==null){
            return;
        }
        Log.d(TAG,"Restoring instances");

        stepNumber = savedInstanceState.getInt(BUNDLE_STEP_ID_KEY);
        recipeId = savedInstanceState.getString(BUNDLE_STEP_RECIPE_ID);
        count = savedInstanceState.getInt(BUNDLE_STEP_COUNT);
    }

    private void replaceFragment(int newPosition){

        StepDetailFragment fragment = new StepDetailFragment();
        fragment.setRecipeId(recipeId);
        fragment.setStepNumber(newPosition);
        fragment.setCount(count);

        getFragmentManager().beginTransaction()
                .replace(R.id.step_detail_frame,fragment)
                .commit();
    }


    private String getOutOfStepNumberString(){
        return "STEP "+(stepNumber+1)+" OF "+count;
    }



    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
