    package com.example.final3

    import androidx.test.core.app.ActivityScenario
    import androidx.test.espresso.Espresso.onView
    import androidx.test.espresso.action.ViewActions.*
    import androidx.test.espresso.assertion.ViewAssertions.matches
    import androidx.test.espresso.matcher.ViewMatchers.*
    import androidx.test.ext.junit.runners.AndroidJUnit4
    import org.junit.Test
    import org.junit.runner.RunWith

    @RunWith(AndroidJUnit4::class)
    class WriteNameActivityTest {

        @Test
        fun testStartButton_navigatesToSelectCharacter() {
            ActivityScenario.launch(WriteNameActivity::class.java)
            onView(withId(R.id.select_nameInput)).perform(typeText("Adam"), closeSoftKeyboard())
            onView(withId(R.id.btnStart)).perform(click())
        }
        @Test
        fun testEmptyName_showsError() {
            ActivityScenario.launch(WriteNameActivity::class.java)
            onView(withId(R.id.btnStart)).perform(click())
            onView(withId(R.id.select_nameFieldLayout))
                .check(matches(hasDescendant(withText("من فضلك أدخل الاسم"))))
        }
    }
