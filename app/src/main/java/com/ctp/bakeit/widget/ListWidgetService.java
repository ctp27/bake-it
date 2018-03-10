package com.ctp.bakeit.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ctp.bakeit.R;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItPreferences;

/**
 * Created by clinton on 3/9/18.
 */

public class ListWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private static final String TAG = ListRemoteViewsFactory.class.getSimpleName();
    private Context context;
    private Cursor mCursor;

    public ListRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        String recipeId = BakeItPreferences.getDisplayRecipeId(context);
        if (mCursor != null) mCursor.close();
        mCursor = context.getContentResolver().query(BakeItContract.IngredientEntry.INGREDIENT_CONTENT_URI,
                null, BakeItContract.IngredientEntry.COLUMN_RECIPE_ID + "=?",
                new String[]{recipeId}, BakeItContract.IngredientEntry._ID + " ASC");
        Log.d(TAG,"Cursor count is "+ mCursor.getCount());
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);
        float quantity = mCursor.getFloat(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_QUANTITY));
        String measure = mCursor.getString(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_MEASURE));
        String ingredName = mCursor.getString(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_NAME));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        views.setTextViewText(R.id.widget_list_item_quantity,quantity+measure);
        views.setTextViewText(R.id.widget_list_item_ingredient_name,ingredName);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
