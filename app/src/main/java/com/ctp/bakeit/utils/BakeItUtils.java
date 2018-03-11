package com.ctp.bakeit.utils;

import android.content.ContentValues;
import android.database.Cursor;

import com.ctp.bakeit.models.Ingredient;
import com.ctp.bakeit.models.Recipe;
import com.ctp.bakeit.models.Step;
import com.ctp.bakeit.provider.BakeItContract;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clinton on 2/24/18.
 */

public class BakeItUtils {

    private static final String LOG = BakeItUtils.class.getSimpleName();


    public static List<Recipe> getRecipeeListFromJson(String jsonString)
                throws JSONException{

        List<Recipe> recipes = new ArrayList<>();

        JSONArray movieResultsArray = new JSONArray(jsonString);

        for(int i=0; i<movieResultsArray.length(); i++) {

            String thisObject = movieResultsArray.getString(i);

            Recipe temp = new Gson().fromJson(thisObject, Recipe.class);

            recipes.add(temp);
        }

        return recipes;

    }


    public static ContentValues populateRecipeContentValue(Recipe recipe){

        ContentValues cv = new ContentValues();
        cv.put(BakeItContract.RecipeEntry.COLUMN_NAME,recipe.getName());
        cv.put(BakeItContract.RecipeEntry.COLUMN_RECIPE_ID,recipe.getId());
        cv.put(BakeItContract.RecipeEntry.COLUMN_IMAGE_URL,recipe.getImage());
        cv.put(BakeItContract.RecipeEntry.COLUMN_SERVINGS,recipe.getServings());
        cv.put(BakeItContract.RecipeEntry.COLUMN_INGREDIENT_COUNT,recipe.getIngredients().size());

        return cv;

    }


    public static ContentValues[] populateIngredientContentValuesArray(List<Ingredient> ingredients, int recipeId){

        ContentValues[] values = new ContentValues[ingredients.size()];

        for(int i=0; i<ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            ContentValues cv = new ContentValues();
            cv.put(BakeItContract.IngredientEntry.COLUMN_QUANTITY, ingredient.getQuantity());
            cv.put(BakeItContract.IngredientEntry.COLUMN_MEASURE, ingredient.getMeasure());
            cv.put(BakeItContract.IngredientEntry.COLUMN_NAME, ingredient.getIngredient());
            cv.put(BakeItContract.IngredientEntry.COLUMN_RECIPE_ID, recipeId);

            values[i] = cv;
        }
        return values;
    }


    public static ContentValues[] populateStepContentValuesArray(List<Step> steps, int recipeId){

        ContentValues[] values = new ContentValues[steps.size()];

        for(int i=0; i<steps.size(); i++) {
            Step step = steps.get(i);
            ContentValues cv = new ContentValues();
            cv.put(BakeItContract.StepEntry.COLUMN_SHORT_DESC, step.getShortDescription());
            cv.put(BakeItContract.StepEntry.COLUMN_DESC, step.getDescription());
            cv.put(BakeItContract.StepEntry.COLUMN_VIDEO_URL, step.getVideoURL());
            cv.put(BakeItContract.StepEntry.COLUMN_THUMBNAIL_URL, step.getThumbnailURL());
            cv.put(BakeItContract.StepEntry.COLUMN_RECIPE_ID,recipeId);
            cv.put(BakeItContract.StepEntry.COLUMN_STEP_NUMBER,i);
            values[i] = cv;
        }
        return values;
    }


    public static String getFormattedDescriptionString(String description, int position){

        final int STRING_THRESHOLD=100;

        String newDescription = null;
        if(position!=0) {
            newDescription = description.substring(3);
        }
        else {
            return description;
        }

        if(newDescription.length()>STRING_THRESHOLD){
            newDescription = newDescription.substring(0,STRING_THRESHOLD);
            newDescription = newDescription +"...";
        }

        return newDescription;
    }

    public static Step getStepFromContentValues(Cursor cursor){

        int stepId = cursor.getInt(cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_STEP_NUMBER));
        String shortDesc = cursor.getString(cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_SHORT_DESC));
        String desc = cursor.getString(cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_DESC));
        String videoUrl = cursor.getString(cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_VIDEO_URL));
        String imageUrl = cursor.getString(cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_THUMBNAIL_URL));

        return new Step(stepId,shortDesc,desc,videoUrl,imageUrl);
    }


    public static String formatIngredientQuantity(String quantity){
        int indexOfDecimal = quantity.indexOf('.');
        int numberAfterDecimal = Integer.parseInt(quantity.substring(indexOfDecimal+1));

        if(numberAfterDecimal==0){
            return quantity.substring(0,indexOfDecimal);
        }
        return quantity;
    }

    public static String formatIngredientMeasure(String measure){

        switch (measure){
            case "G":
                return "g";
            case "KG":
                return "kg";
            case "UNIT":
                return "";
            case "TBLSP":
                return "tbsp";
            case "TSP":
                return "tsp";
            case "CUP":
                return "cup";
            case "OZ":
                return "oz";
            default:
                return measure;
        }

    }

    public static String getFormattedIngredientName(String ingredientName){
        return ingredientName.substring(0, 1).toUpperCase() + ingredientName.substring(1);
    }



}
