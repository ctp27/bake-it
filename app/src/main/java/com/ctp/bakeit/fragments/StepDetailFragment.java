package com.ctp.bakeit.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctp.bakeit.R;
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

    @BindView(R.id.simpleExoPlayerView)
    SimpleExoPlayerView mSimpleExoPlayerView;

    @BindView(R.id.step_thumbnail)
    ImageView thumbnailImage;

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
    private long playbackPosition;
    private int currentWindow;
    private boolean isPlayReady = true;

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;



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
//        mCallback.onNextBtnClicked(newStep);
        replaceFragment(newStep);
    }

    private void onPrevBtnClicked(){
        int newStep = stepNumber -1;
//        mCallback.onPrevBtnClicked(newStep);
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

                    if(!isLandscape){
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

    private void initializeImageOrVideoView(Step step){

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
            Log.d(TAG, "Current window is "+currentWindow+" | Playback position is "+playbackPosition);
            mExoPlayer.setPlayWhenReady(isPlayReady);
            mExoPlayer.seekTo(currentWindow,playbackPosition);
            mExoPlayer.addListener(this);

        }

        MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("Baking Instruction Video"))
                .createMediaSource(mediaUri);
        mExoPlayer.prepare(mediaSource, true, false);
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
             } else if((playbackState == ExoPlayer.STATE_READY)){
                        Log.d(TAG, "onPlayerStateChanged: PAUSED");
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
            }

         mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification(mStateBuilder.build());
    }

    private void showNotification(PlaybackStateCompat state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());

        int icon;
        String play_pause;
        if(state.getState() == PlaybackStateCompat.STATE_PLAYING){
            icon = R.drawable.exo_controls_pause;
            play_pause = "Pause";
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = "Play";
        }


        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(getContext(),
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new android.support.v4.app.NotificationCompat
                .Action(R.drawable.exo_controls_previous, "Restart",
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (getContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (getContext(), 0, new Intent(getContext(), StepDetailActivity.class), 0);

        builder.setContentTitle("Media Player")
                .setContentText("Step "+(stepNumber+1))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0,1));


        mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
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
            isPlayReady = mExoPlayer.getPlayWhenReady();
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
            mNotificationManager.cancelAll();
            mExoPlayer.release();
            mExoPlayer = null;
            mMediaSession.setActive(false);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"On stop called");
        releaseResources();
    }
}
