package com.ctp.bakeit.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by clinton on 2/23/18.
 */

public class BakeItProvider extends ContentProvider {

    private static final int CODE_RECIPE=101;
    private static final int CODE_RECIPE_WITH_ID=102;
    private static final int CODE_STEP=201;
    private static final int CODE_STEP_WITH_ID = 202;
    private static final int CODE_INGREDIENT=301;


    private BakeItOpenHelper mOpenHelper;

    private UriMatcher mUriMatcher = buildUriMatcher();


    @Override
    public boolean onCreate() {
        mOpenHelper = new BakeItOpenHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = mUriMatcher.match(uri);

        switch (match){
            case CODE_STEP:
                return bulkInsertIntoTable(db,values,uri, BakeItContract.StepEntry.TABLE_NAME);
            case CODE_INGREDIENT:
                return bulkInsertIntoTable(db,values,uri, BakeItContract.IngredientEntry.TABLE_NAME);
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor = null;

        switch (mUriMatcher.match(uri)){
            case CODE_RECIPE:
                cursor = queryTable(BakeItContract.RecipeEntry.TABLE_NAME,projection,selection,
                        selectionArgs,sortOrder);
                break;
            case CODE_RECIPE_WITH_ID:
                cursor = queryTableWithId(BakeItContract.RecipeEntry.TABLE_NAME, BakeItContract.RecipeEntry._ID,
                        uri,projection,sortOrder);
                break;
            case CODE_STEP:
                cursor = queryTable(BakeItContract.StepEntry.TABLE_NAME,projection,selection,
                        selectionArgs,sortOrder);
                break;

            case CODE_STEP_WITH_ID:
                cursor = queryTableWithId(BakeItContract.StepEntry.TABLE_NAME, BakeItContract.StepEntry._ID,
                        uri,projection,sortOrder);
                break;

            case CODE_INGREDIENT:
                cursor = queryTable(BakeItContract.IngredientEntry.TABLE_NAME,projection,selection,
                        selectionArgs,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

       final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       Uri returnUri = null;

       switch (mUriMatcher.match(uri)){
           case CODE_RECIPE:
               long id = db.insert(BakeItContract.RecipeEntry.TABLE_NAME, null, values);
               if ( id > 0 ) {
                   returnUri = ContentUris.withAppendedId(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,id);
               } else {
                   throw new android.database.SQLException("Failed to insert row into " + uri);
               }
               break;
           // Set the value for the returnedUri and write the default case for unknown URI's
           // Default case throws an UnsupportedOperationException
           default:
               throw new UnsupportedOperationException("Unknown uri: " + uri);
       }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;

    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted = 0;
        if (null == selection) selection = "1";

        switch (mUriMatcher.match(uri)) {

            case CODE_RECIPE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        BakeItContract.RecipeEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case CODE_STEP:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        BakeItContract.StepEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case CODE_INGREDIENT:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        BakeItContract.IngredientEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    private static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BakeItContract.AUTHORITY;
        matcher.addURI(authority, BakeItContract.PATH_RECIPE, CODE_RECIPE);
        matcher.addURI(authority, BakeItContract.PATH_RECIPE + "/#", CODE_RECIPE_WITH_ID);
        matcher.addURI(authority,BakeItContract.PATH_STEP,CODE_STEP);
        matcher.addURI(authority,BakeItContract.PATH_STEP+"/#",CODE_STEP_WITH_ID);
        matcher.addURI(authority,BakeItContract.PATH_INGREDIENTS,CODE_INGREDIENT);
        return matcher;
    }

    private int bulkInsertIntoTable(SQLiteDatabase db, ContentValues[] values, Uri uri,String tableName){

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsInserted;
    }



    private Cursor queryTable(String tableName, String[] projection,
                              @Nullable String selection, @Nullable String[] selectionArgs,
                              @Nullable String sortOrder){


        return mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private Cursor queryTableWithId(String tableName,String tableIdColumnName, Uri uri,String[] projection,
                                    @Nullable String sortOrder){


        String id = uri.getLastPathSegment();
        String[] selectionArguments = new String[]{id};

        return mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                tableName,
                projection,
                tableIdColumnName + " = ? ",
                selectionArguments,
                null,
                null,
                sortOrder);


    }
}
