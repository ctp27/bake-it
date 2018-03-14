package com.ctp.bakeit;

import android.net.Uri;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.ctp.bakeit.provider.BakeItContract;

import org.hamcrest.core.AllOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;

/**
 * Created by clinton on 3/11/18.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityRecipeClickTest {

    private Uri uri =  BakeItContract.RecipeEntry.getRecipeContentUriForId(2);
    private IdlingResource mIdlingResource;

    @Rule
    public IntentsTestRule<MainActivity> mainActivityActivityTestRule =
            new IntentsTestRule<>(MainActivity.class);


    @Before
    public void registerIdlingResource() {
        mIdlingResource = mainActivityActivityTestRule.getActivity().getIdlingResource();
        Espresso.registerIdlingResources(mIdlingResource);

    }

    @Test
    public void clickListItem_OpensRecipeDetails(){

        onView(ViewMatchers.withId(R.id.recipes_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1,click()));

        Intents.intended(AllOf.allOf(
                IntentMatchers.hasData(uri),
                IntentMatchers.toPackage("com.ctp.bakeit")
                ));


    }

    @After
    public void unregisterIdlingResource(){
        if (mIdlingResource != null) {
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }

}
