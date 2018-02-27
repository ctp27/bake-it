package com.ctp.bakeit.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by clinton on 2/23/18.
 */

public class BakeItContract {

    public static final String AUTHORITY = "com.ctp.bakeit";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);


    public static final String PATH_RECIPE = "recipe";

    public static final String PATH_STEP = "steps";

    public static final String PATH_INGREDIENTS = "ingredients";



    public static final class RecipeEntry implements BaseColumns{

        public static final Uri RECIPE_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RECIPE).build();

        public static Uri getRecipeContentUriForId(int id){
            return RECIPE_CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
        }

        public static final String TABLE_NAME = "recipe";

        public static final String COLUMN_RECIPE_ID = "recId";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_SERVINGS = "servings";

        public static final String COLUMN_IMAGE_URL = "image";

        public static final String COLUMN_INGREDIENT_COUNT = "ingredientCount";

    }

    public static final class StepEntry implements BaseColumns{

        public static final Uri STEP_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STEP).build();


        public static Uri getStepContentUriForId(int id){
            return STEP_CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
        }

        public static final String TABLE_NAME = "steps";

        public static final String COLUMN_SHORT_DESC = "shortDescription";

        public static final String COLUMN_DESC = "description";

        public static final String COLUMN_VIDEO_URL = "videoUrl";

        public static final String COLUMN_THUMBNAIL_URL = "imageUrl";

        public static final String COLUMN_RECIPE_ID = "recId";

        public static final String COLUMN_STEP_NUMBER = "stepNumber";

    }


    public static final class IngredientEntry implements BaseColumns{

        public static final Uri INGREDIENT_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_INGREDIENTS).build();

        public static final String TABLE_NAME = "ingredients";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_MEASURE = "measure";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_RECIPE_ID = "recId";

    }

}
