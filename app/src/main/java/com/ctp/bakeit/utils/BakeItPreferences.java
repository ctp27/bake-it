package com.ctp.bakeit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by clinton on 3/9/18.
 */

public class BakeItPreferences {

    public static final String DEFAULT_VALUE = "empty";
    private static final String PREF_RECIPE_KEY="recipe_id_key";
    private static final String PREF_RECIPE_NAME = "recipe_name_key";

    public static void insertWidgetRecipeDetails(Context context, String id, String recipeName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_RECIPE_KEY, id);
        editor.putString(PREF_RECIPE_NAME,recipeName);
        editor.apply();
    }

    public static String getDisplayRecipeId(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_RECIPE_KEY,DEFAULT_VALUE);
    }


    public static String getDisplayRecipeName(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_RECIPE_NAME,DEFAULT_VALUE);
    }


}
