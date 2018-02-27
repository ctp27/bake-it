package com.ctp.bakeit.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by clinton on 2/23/18.
 */

public class BakeItOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "bakeIt.db";

    public BakeItOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE_RECIPE = "CREATE TABLE "  + BakeItContract.RecipeEntry.TABLE_NAME + " (" +
                BakeItContract.RecipeEntry._ID                + " INTEGER PRIMARY KEY, " +
                BakeItContract.RecipeEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                BakeItContract.RecipeEntry.COLUMN_NAME + " TEXT NOT NULL, "+
                BakeItContract.RecipeEntry.COLUMN_SERVINGS+" REAL NOT NULL, "+
                BakeItContract.RecipeEntry.COLUMN_INGREDIENT_COUNT +" INTEGER NOT NULL, "+
                BakeItContract.RecipeEntry.COLUMN_IMAGE_URL+" TEXT);";

        final String CREATE_TABLE_STEPS = "CREATE TABLE "  + BakeItContract.StepEntry.TABLE_NAME + " (" +
                BakeItContract.StepEntry._ID                + " INTEGER PRIMARY KEY, " +
                BakeItContract.StepEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                BakeItContract.StepEntry.COLUMN_STEP_NUMBER +" INTEGER NOT NULL, "+
                BakeItContract.StepEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "+
                BakeItContract.StepEntry.COLUMN_DESC + " TEXT NOT NULL, "+
                BakeItContract.StepEntry.COLUMN_VIDEO_URL + " TEXT, " +
                BakeItContract.StepEntry.COLUMN_THUMBNAIL_URL + " TEXT);";

        final String CREATE_TABLE_INGREDIENTS = "CREATE TABLE "  + BakeItContract.IngredientEntry.TABLE_NAME + " (" +
                BakeItContract.IngredientEntry._ID                + " INTEGER PRIMARY KEY, " +
                BakeItContract.IngredientEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                BakeItContract.IngredientEntry.COLUMN_QUANTITY + " REAL NOT NULL, "+
                BakeItContract.IngredientEntry.COLUMN_MEASURE + " TEXT NOT NULL, "+
                BakeItContract.IngredientEntry.COLUMN_NAME + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_STEPS);
        db.execSQL(CREATE_TABLE_INGREDIENTS);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BakeItContract.RecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BakeItContract.StepEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BakeItContract.IngredientEntry.TABLE_NAME);
        onCreate(db);
    }


}
