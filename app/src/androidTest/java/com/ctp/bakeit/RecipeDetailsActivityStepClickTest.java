package com.ctp.bakeit;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ctp.bakeit.provider.BakeItContract;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by clinton on 3/13/18.
 */

@RunWith(AndroidJUnit4.class)
public class RecipeDetailsActivityStepClickTest {

    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<RecipeDetailsActivity> mActivityRule =
            new ActivityTestRule<>(RecipeDetailsActivity.class, false, false);


    /**
     * Opens the recipe Detail activity for brownies. On clicking the first recipe step,
     * it performs an intent verification in case of a phone or checks if the view is updated
     * in case of a tablet
     */

    @Test
    public void testOnStepClickCreatesIntent(){
        Intent i = new Intent();
        i.setData(BakeItContract.RecipeEntry.getRecipeContentUriForId(2));
        mActivityRule.launchActivity(i);
        mIdlingResource = mActivityRule.getActivity().getIdlingResource();
        Espresso.registerIdlingResources(mIdlingResource);

        onView(ViewMatchers.withId(R.id.recipe_details_steps_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));

        if(!mActivityRule.getActivity().getResources().getBoolean(R.bool.isTablet)){
            Intents.intended(AllOf.allOf(
                    IntentMatchers.hasExtra(StepDetailActivity.INTENT_RECIPE_NAME_EXTRA,"Brownies"),
                    IntentMatchers.hasExtra(StepDetailActivity.INTENT_RECIPE_ID_EXTRA,2),
                    IntentMatchers.hasExtra(StepDetailActivity.INTENT_RECIPE_STEP_NUMBER,0),
                    IntentMatchers.toPackage("com.ctp.bakeit")));
        }
        else{
            onView(withId(R.id.step_detail_short_description)).check(matches(withText("Recipe Introduction")));
        }


    }

}
