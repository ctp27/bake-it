package com.ctp.bakeit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.RecipeDetailsActivity;
import com.ctp.bakeit.StepDetailActivity;
import com.ctp.bakeit.models.Step;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItUtils;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/26/18.
 */

public class StepDetailFragment extends Fragment
                implements LoaderManager.LoaderCallbacks<Cursor>,
                ExoPlayer.EventListener{

    private static final String TAG = StepDetailFragment.class.getSimpleName();
    private static final int CURSOR_STEP_LOADER=101;

    private static final String BUNDLE_STEP_ID_KEY="key-step_id";
    private static final String BUNDLE_STEP_RECIPE_ID="key-recipe_id";
    private static final String BUNDLE_STEP_COUNT="key-count";
    private static final String BUNDLE_PLAYBACK_POSITION_KEY="exo-playback-key";
    private static final String BUNDLE_CURRENT_WINDOW_KEY="exo-window-key";
    private static final String BUNDLE_IS_PLAY_READY_KEY="exo-play-ready";


    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    @BindBool(R.bool.isPortrait)
    boolean isPortrait;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    @BindView(R.id.simpleExoPlayerView)
    SimpleExoPlayerView mSimpleExoPlayerView;

    @BindView(R.id.step_thumbnail)
    ImageView thumbnailImage;

    @BindView(R.id.no_internet_message)
    TextView errorTextView;

    @BindView(R.id.refresh_media_btn)
    Button refreshBtn;

    private ConstraintLayout landscapeConstraintLayout;

    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private TextView stepNumberView;
    private TextView shortDescriptionView;
    private TextView descriptionView;

    private StepDetailFragmentCallback mCallback;


    private SimpleExoPlayer mExoPlayer;
    private int stepNumber;
    private String recipeId;
    private int count;
    private long playbackPosition;
    private int currentWindow;
    private boolean isPlayReady;

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;



    public interface StepDetailFragmentCallback{
        void onNextBtnClicked(int stepNumber);
        void onPrevBtnClicked(int stepNumber);
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

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().restartLoader(CURSOR_STEP_LOADER,null,StepDetailFragment.this);
            }
        });




        return rootView;
    }

    private void initializePortraidModeWidgets(View rootView) {

        if(isPortrait || isTablet){
            nextBtn = rootView.findViewById(R.id.step_detail_nxt_btn);
            prevBtn = rootView.findViewById(R.id.step_detail_prev_btn);
            stepNumberView = rootView.findViewById(R.id.step_detail_step_number_view);
            shortDescriptionView = rootView.findViewById(R.id.step_detail_short_description);
            descriptionView = rootView.findViewById(R.id.step_detail_description);

            if(isTablet){
                nextBtn.setVisibility(View.INVISIBLE);
                prevBtn.setVisibility(View.INVISIBLE);
            }
            else {
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

                if (stepNumber == count - 1) {
                    nextBtn.setEnabled(false);
                    nextBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_grey));
                } else {
                    nextBtn.setEnabled(true);
                }

                if (stepNumber == 0) {
                    prevBtn.setEnabled(false);
                    prevBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_grey));
                } else
                    prevBtn.setEnabled(true);
            }
        }
        else {
            landscapeConstraintLayout = rootView.findViewById(R.id.land_error_constraints);
        }
    }

    private void onNextBtnClicked() {
        int newStep=stepNumber+1;
        mCallback.onNextBtnClicked(newStep);
        replaceFragment(newStep);
    }

    private void onPrevBtnClicked(){
        int newStep = stepNumber -1;
        mCallback.onPrevBtnClicked(newStep);
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

        switch (loader.getId()){
            case CURSOR_STEP_LOADER:
                if(data!=null){
                    data.moveToFirst();

                    Step thisStep = BakeItUtils.getStepFromContentValues(data);
                    String description=null;

                    if(thisStep.getId()!=0) {
                        description = thisStep.getDescription().substring(3);
                    }else {
                        description = thisStep.getDescription();
                    }

                    if(isPortrait || isTablet){
                        shortDescriptionView.setText(thisStep.getShortDescription());
                        descriptionView.setText(description);
                        stepNumberView.setText(getOutOfStepNumberString());
                    }

                    initializeImageOrVideoView(thisStep);
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    private void initializeImageOrVideoView(Step step){

        if(!BakeItUtils.isConnectedToInternet(getContext())){
            displayErrorMessage();
            return;
        }

        hideErrorMessage();

        String videoUrl = step.getVideoURL();
        String imgUrl = step.getThumbnailURL();

        if(videoUrl!=null && !videoUrl.trim().isEmpty()){
            Uri mediaUri = Uri.parse(videoUrl);
            initializeMediaSession();
            initializeExoPlayer(mediaUri);
        }
        else {
            setImageResource(imgUrl);

        }

    }

    private void displayErrorMessage() {
        mSimpleExoPlayerView.setVisibility(View.INVISIBLE);
        if(isLandscape){
            landscapeConstraintLayout.setVisibility(View.VISIBLE);
        }
        errorTextView.setVisibility(View.VISIBLE);
        refreshBtn.setVisibility(View.VISIBLE);
    }



    private void hideErrorMessage(){
        mSimpleExoPlayerView.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.INVISIBLE);
        refreshBtn.setVisibility(View.INVISIBLE);
        if(isLandscape){
            landscapeConstraintLayout.setVisibility(View.INVISIBLE);
        }
    }



    private void setImageResource(String imgUrl){
        mSimpleExoPlayerView.setVisibility(View.INVISIBLE);
        thumbnailImage.setVisibility(View.VISIBLE);
        if(imgUrl!=null && !imgUrl.trim().isEmpty()) {

            Picasso.with(getContext()).load(imgUrl)
                    .placeholder(R.drawable.cupcake_logo)
                    .error(R.drawable.cupcake_logo)
                    .into(thumbnailImage);
        }else {
            thumbnailImage.setImageResource(R.drawable.cupcake_logo);
        }
    }



    private void initializeExoPlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getContext()),
                    trackSelector,loadControl);
            mSimpleExoPlayerView.setPlayer(mExoPlayer);
            Log.d(TAG, "isPlay ready is "+isPlayReady);
            mExoPlayer.setPlayWhenReady(isPlayReady);

            mExoPlayer.addListener(this);

        }

        MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("Baking Instruction Video"))
                .createMediaSource(mediaUri);
        mExoPlayer.prepare(mediaSource, true, false);
        mExoPlayer.seekTo(currentWindow,playbackPosition);
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
               Log.d(TAG, "onPlayerStateChanged: PLAYING");
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
                isPlayReady = true;
             } else if((playbackState == ExoPlayer.STATE_READY)){
                        Log.d(TAG, "onPlayerStateChanged: PAUSED");
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
                isPlayReady = false;
            }

         mMediaSession.setPlaybackState(mStateBuilder.build());
    }


    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(getContext(), TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }

    private class MySessionCallback extends MediaSessionCompat.Callback{


        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }


    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"Saving instances");
        if(mExoPlayer!=null) {
            playbackPosition = mExoPlayer.getCurrentPosition();
            currentWindow = mExoPlayer.getCurrentWindowIndex();
        }

        outState.putInt(BUNDLE_STEP_ID_KEY, stepNumber);
        outState.putString(BUNDLE_STEP_RECIPE_ID,recipeId);
        outState.putInt(BUNDLE_STEP_COUNT,count);
        outState.putLong(BUNDLE_PLAYBACK_POSITION_KEY,playbackPosition);
        outState.putInt(BUNDLE_CURRENT_WINDOW_KEY,currentWindow);
        outState.putBoolean(BUNDLE_IS_PLAY_READY_KEY,isPlayReady);
    }

    private void restoreVariablesIfSavedBundle(Bundle savedInstanceState){

        if(savedInstanceState==null){
            playbackPosition=0;
            currentWindow=0;
            isPlayReady = true;
            return;
        }
        Log.d(TAG,"Restoring instances");

        stepNumber = savedInstanceState.getInt(BUNDLE_STEP_ID_KEY);
        recipeId = savedInstanceState.getString(BUNDLE_STEP_RECIPE_ID);
        count = savedInstanceState.getInt(BUNDLE_STEP_COUNT);
        playbackPosition = savedInstanceState.getLong(BUNDLE_PLAYBACK_POSITION_KEY);
        currentWindow = savedInstanceState.getInt(BUNDLE_CURRENT_WINDOW_KEY);
        isPlayReady = savedInstanceState.getBoolean(BUNDLE_IS_PLAY_READY_KEY);
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


    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
   }

    private void releaseResources(){
        if(mExoPlayer!=null) {
            mExoPlayer.release();
            mExoPlayer = null;
            mMediaSession.setActive(false);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            getLoaderManager().restartLoader(CURSOR_STEP_LOADER,null,this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
           getLoaderManager().restartLoader(CURSOR_STEP_LOADER,null,this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"On stop called");
        if (Util.SDK_INT > 23) {
            releaseResources();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releaseResources();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (StepDetailActivity) context;
        } catch (ClassCastException e) {
            try{
                mCallback = (RecipeDetailsActivity) context;
            }
            catch (ClassCastException f){
                throw new ClassCastException(context.toString()
                        + " must implement OnImageClickListener");
            }

        }
    }

}
