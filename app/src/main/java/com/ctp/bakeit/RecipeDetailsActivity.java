package com.ctp.bakeit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ctp.bakeit.fragments.RecipeDetailsFragment;

public class RecipeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        Intent recievedIntent = getIntent();
        Uri uri = null;
        if(recievedIntent!=null){
            uri = recievedIntent.getData();
        }

        RecipeDetailsFragment fragment = new RecipeDetailsFragment();
        fragment.setQueryUri(uri);

        getSupportFragmentManager().beginTransaction().
                add(R.id.recipe_details_fragment,fragment)
                .commit();

    }
}
