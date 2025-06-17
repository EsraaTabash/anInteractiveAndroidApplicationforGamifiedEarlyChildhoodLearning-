package com.example.final3
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CreateAccountActivityUITest {

    @Test
    fun testValidateFields_EmptyEmail() {
        val scenario = ActivityScenario.launch(CreateAccountActivity::class.java)
        onView(withId(R.id.create_emailInput)).check(matches(isDisplayed()))
        onView(withId(R.id.create_emailInput)).perform(typeText(""), closeSoftKeyboard())
        onView(withId(R.id.create_passwordInput)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.create_confirmPasswordInput)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn)).perform(click())

        onView(withId(R.id.create_emailFieldLayout))
            .check(matches(hasDescendant(withText("البريد الإلكتروني مطلوب"))))
    }

    @Test
    fun testValidateFields_InvalidEmail() {
        val scenario = ActivityScenario.launch(CreateAccountActivity::class.java)
        onView(withId(R.id.create_emailInput)).check(matches(isDisplayed()))

        onView(withId(R.id.create_emailInput)).perform(typeText("invalid-email"), closeSoftKeyboard())
        onView(withId(R.id.create_passwordInput)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.create_confirmPasswordInput)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn)).perform(click())

        onView(withId(R.id.create_emailFieldLayout))
            .check(matches(hasDescendant(withText("صيغة البريد الإلكتروني غير صحيحة"))))
    }

    @Test
    fun testValidateFields_ShortPassword() {
        val scenario = ActivityScenario.launch(CreateAccountActivity::class.java)
        onView(withId(R.id.create_passwordInput)).check(matches(isDisplayed()))

        onView(withId(R.id.create_emailInput)).perform(typeText("user@example.com"), closeSoftKeyboard())
        onView(withId(R.id.create_passwordInput)).perform(typeText("pass"), closeSoftKeyboard()) // كلمة مرور قصيرة جداً
        onView(withId(R.id.create_confirmPasswordInput)).perform(typeText("pass"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn)).perform(click())

        onView(withId(R.id.create_passwordFieldLayout))
            .check(matches(hasDescendant(withText("كلمة المرور قصيرة جدًا"))))
    }

    @Test
    fun testValidateFields_PasswordMismatch() {
        val scenario = ActivityScenario.launch(CreateAccountActivity::class.java)
        onView(withId(R.id.create_confirmPasswordInput)).check(matches(isDisplayed()))

        onView(withId(R.id.create_emailInput)).perform(typeText("user@example.com"), closeSoftKeyboard())
        onView(withId(R.id.create_passwordInput)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.create_confirmPasswordInput)).perform(typeText("differentpassword"), closeSoftKeyboard()) // كلمات مرور غير متطابقة

        onView(withId(R.id.createAccountBtn)).perform(click())

        onView(withId(R.id.create_confirmPasswordFieldLayout))
            .check(matches(hasDescendant(withText("كلمات المرور غير متطابقة"))))
    }

    @Test
    fun testValidateFields_AllValid() {
        val scenario = ActivityScenario.launch(CreateAccountActivity::class.java)
        onView(withId(R.id.create_emailInput)).check(matches(isDisplayed()))

        onView(withId(R.id.create_emailInput)).perform(typeText("user@example.com"), closeSoftKeyboard())
        onView(withId(R.id.create_passwordInput)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.create_confirmPasswordInput)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn)).perform(click())

    }
}
