package com.ctp.bakeit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.ctp.bakeit.MainActivity;
import com.ctp.bakeit.R;
import com.ctp.bakeit.RecipeDetailsActivity;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItPreferences;

/**
 * Implementation of App Widget functionality.
 */
public class IngredientListWidget extends AppWidgetProvider {

    private static final String TAG = IngredientListWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String recipeName, String recipeId) {

        // Construct the RemoteViews object
        RemoteViews views = getListRemoteView(context,recipeName,recipeId);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d(TAG,"OnUpdate called");
        IngredientWidgetService.startServiceUpdateWidget(context);
    }

    public static void updateIngredientWidgets(Context context, AppWidgetManager appWidgetManager,
                                               String recipeName, String recipeId, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId,recipeName,recipeId);
        }
    }

    private static RemoteViews getListRemoteView(Context context, String recipeName, String recipeId){

        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.new_app_widget);
        // Set the GridWidgetService intent to act as the adapter for the GridView



        Intent appIntent = null;
        PendingIntent appPendingIntent = null;

        if(recipeName.equals(BakeItPreferences.DEFAULT_VALUE)){
            appIntent = new Intent(context, MainActivity.class);
            recipeName = "Add a recipe!";
            appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            appIntent = new Intent(context,RecipeDetailsActivity.class);
            appIntent.setData(ContentUris.withAppendedId(BakeItContract.RecipeEntry.RECIPE_CONTENT_URI,Long.parseLong(recipeId)));
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(appIntent);
            appPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent intent = new Intent(context, ListWidgetService.class);
            views.setRemoteAdapter(R.id.widget_ingredient_list_text_view, intent);
            views.setEmptyView(R.id.widget_ingredient_list_text_view, R.id.widget_default_view);
        }
        views.setTextViewText(R.id.widget_title,recipeName);
        views.setOnClickPendingIntent(R.id.widget_whole_layout, appPendingIntent);
        // Handle empty gardens

        return views;

    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

