package com.ctp.bakeit.sync;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.ctp.bakeit.models.Recipe;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItUtils;
import com.ctp.bakeit.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by clinton on 2/27/18.
 */

public class BakeSyncTask {

    private static final String LOG_TAG = BakeSyncTask.class.getSimpleName();


    synchronized public static void syncRecipes(Context context){

        String response = null;
        try {
            response = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildJsonRecipeUrl());
        }
        catch (IOException e){

        }
        if(response==null){
            return;
        }

        List<Recipe> recipes = null;

        try {
            recipes = BakeItUtils.getRecipeeListFromJson(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(recipes == null){
            Log.d(LOG_TAG,"recipees is null");
            return;
        }
        Log.d("YOURS TRULY","Syncing in progress");
        context.getContentResolver().delete(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,null,null);
        context.getContentResolver().delete(BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,null,null);
        context.getContentResolver().delete(BakeItContract.StepEntry.STEP_CONTENT_URI,null,null);


        for(Recipe recipe: recipes){

            ContentValues recipeEntry = BakeItUtils.populateRecipeContentValue(recipe);
            Uri returnUri = context.getContentResolver().insert(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,recipeEntry);
            String id = returnUri.getLastPathSegment();
            ContentValues[] ingredientValues = BakeItUtils.populateIngredientContentValuesArray(
                    recipe.getIngredients(),Integer.parseInt(id));

            ContentValues[] stepValues = BakeItUtils.populateStepContentValuesArray(
                    recipe.getSteps(),Integer.parseInt(id));

            context.getContentResolver()
                    .bulkInsert(BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,ingredientValues);

            context.getContentResolver()
                    .bulkInsert(BakeItContract.StepEntry.STEP_CONTENT_URI,stepValues);

        }

    }


}
