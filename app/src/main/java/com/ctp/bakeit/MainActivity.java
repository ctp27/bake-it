package com.ctp.bakeit;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ctp.bakeit.adapters.RecipeAdapter;
import com.ctp.bakeit.idlingresource.TestIdlingResource;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.sync.BakeItSyncUtils;

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
    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.refresh_btn)
    ImageButton refreshBtn;

    @Nullable
    private TestIdlingResource mIdlingResource;



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


        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BakeItSyncUtils.startImmediateSync(MainActivity.this);
            }
        });

        BakeItSyncUtils.initialize(this);
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case RECIPE_CURSOR_LOADER_ID:
                if(mIdlingResource!=null){
                    mIdlingResource.setIdleState(false);
                }
                return new CursorLoader(this, BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,
                        null,null,null, BakeItContract.RecipeEntry.COLUMN_RECIPE_ID+" ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        if(data==null || data.getCount()==0){
            displayConnectivityErrorMessage();
            return;
        }

        hideConnectivityErrorMessage();
        
        if(mRecipeAdapter==null) {
            mRecipeAdapter = new RecipeAdapter(data, this);
            recipeRecyclerView.setAdapter(mRecipeAdapter);
        }
        else {
            mRecipeAdapter.swapCursor(data);
        }

        if(mIdlingResource!=null){
            mIdlingResource.setIdleState(true);
        }
    }

    private void hideConnectivityErrorMessage() {
        errorTextView.setVisibility(View.INVISIBLE);
        refreshBtn.setVisibility(View.INVISIBLE);
        recipeRecyclerView.setVisibility(View.VISIBLE);
    }

    private void displayConnectivityErrorMessage() {
        recipeRecyclerView.setVisibility(View.INVISIBLE);
        errorTextView.setVisibility(View.VISIBLE);
        refreshBtn.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(RECIPE_CURSOR_LOADER_ID,null, this);
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new TestIdlingResource();
        }
        return mIdlingResource;
    }

}
