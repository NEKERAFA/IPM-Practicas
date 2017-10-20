package es.udc.ipm.p2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.core.deps.guava.base.Strings;
import android.support.test.rule.ActivityTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.Display;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddCategoryEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void categoryAddTest() {
        // Paso 1: Se pulsa el botón de añadir
        onView(withId(R.id.action_add)).perform(click());
        // Paso 2: Se escribe el nombre de la nueva categoría
        onView(withId(R.id.category_add)).perform(typeText("Baaaaa"));
        // Paso 3: Se pulsa en aceptar
        onView(withText("Add")).perform(click());
        // Paso 4: Se comprueba que ha sido añadido
        onData(anything()).inAdapterView(withId(R.id.mainlist)).atPosition(0)
                .check(matches(withText("Baaaaa")));
        // Paso 5: Se pulsa el botón de añadir
        onView(withId(R.id.action_add)).perform(click());
        // Paso 6: Se escribe el nombre de la nueva categoría
        onView(withId(R.id.category_add)).perform(typeText("Aaaaaa"));
        // Paso 7: Se pulsa en aceptar
        onView(withText("Add")).perform(click());
        // Paso 8: Se comprueba que ha sido añadido
        onData(anything()).inAdapterView(withId(R.id.mainlist)).atPosition(0)
                .check(matches(withText("Aaaaaa")));
        onData(anything()).inAdapterView(withId(R.id.mainlist)).atPosition(1)
                .check(matches(withText("Baaaaa")));
        // Paso 9: Se reestablece el estado
        onData(anything()).inAdapterView(withId(R.id.mainlist)).atPosition(0)
                .perform(longClick());
        onData(anything()).inAdapterView(withId(R.id.mainlist)).atPosition(1)
                .perform(longClick());
        onView(withId(R.id.action_delete)).perform(click());
        onView(withText("Delete")).perform(click());
    }
}
