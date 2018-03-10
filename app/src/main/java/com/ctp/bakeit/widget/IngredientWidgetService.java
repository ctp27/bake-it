package com.ctp.bakeit.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ctp.bakeit.R;
import com.ctp.bakeit.utils.BakeItPreferences;

/**
 * Created by clinton on 3/9/18.
 */

public class IngredientWidgetService extends IntentService {

    public static final String ACTION_UPDATE_INGREDIENT_WIDGET = "update-plant-widget";
    public static final String ACTION_SET_WIDGET = "set-widget-action";
    private static final String INTENT_RECIPE_ID_EXTRA = "recipe_id_extra";
    private static final String TAG = IngredientWidgetService.class.getSimpleName();
    private static final String INTENT_RECIPE_NAME_EXTRA = "recipe_name_extra";

    public IngredientWidgetService() {
        super(IngredientWidgetService.class.getSimpleName());
    }


    public static void startServiceUpdateWidget(Context context){
        Intent intent = new Intent(context,IngredientWidgetService.class);
        intent.setAction(ACTION_UPDATE_INGREDIENT_WIDGET);
        context.startService(intent);
    }

    public static void startServiceSetWidget(Context context, String recipeId, String recipeName){
        Intent intent = new Intent(context,IngredientWidgetService.class);
        intent.setAction(ACTION_SET_WIDGET);
        intent.putExtra(INTENT_RECIPE_ID_EXTRA,recipeId);
        intent.putExtra(INTENT_RECIPE_NAME_EXTRA,recipeName);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()){
            case ACTION_UPDATE_INGREDIENT_WIDGET:
                handleActionUpdateWidget();
                break;
            case ACTION_SET_WIDGET:
                handleActionSetWidget(intent.getStringExtra(INTENT_RECIPE_ID_EXTRA),
                        intent.getStringExtra(INTENT_RECIPE_NAME_EXTRA));
                break;
        }
    }
    

    private void handleActionUpdateWidget(){
        Log.d(TAG,"entered handle Action update widget");
        String recipeName = BakeItPreferences.getDisplayRecipeName(this);
        String id = BakeItPreferences.getDisplayRecipeId(this);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, IngredientListWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_ingredient_list_text_view);
        IngredientListWidget.updateIngredientWidgets(this,appWidgetManager,recipeName,id,appWidgetIds);

    }

    private void handleActionSetWidget(String id, String recipeName){
        BakeItPreferences.insertWidgetRecipeDetails(this,id,recipeName);
        Log.d(TAG,"handleActionSetWidget Called");
        handleActionUpdateWidget();
    }
}
