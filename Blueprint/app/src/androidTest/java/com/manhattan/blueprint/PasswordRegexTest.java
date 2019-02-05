package com.manhattan.blueprint;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PasswordRegexTest {
    private Pattern pattern;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        this.pattern = Pattern.compile(appContext.getResources().getString(R.string.password_regex));
    }

    @Test
    public void testEmpty() {
        assertFalse(pattern.matcher("").matches());
    }

    @Test
    public void testTooShortPasswords() {
        // Bound condition is 4 (should accept 5)
        String[] list = new String[]{"a", "A", "1", "aa", "aaa", "aaaa", "AaAa", "a4pl"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testTooLongPasswords() {
        // Bound condition is 17 (should accept 16)
        String[] list = new String[]{"abcdefghijklmnopq", "4bCd3fGhIjKlMn0pQr", "thisismyverylongpassw0rd"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testNoDigitPasswords() {
        String[] list = new String[]{"abcde", "AbCdefGhIjKlMn", "helloworld"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testNoLowerCasePasswords() {
        String[] list = new String[]{"ABCDE", "A3C034GHIJK1MN", "09812319"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testNoUpperCasePasswords() {
        String[] list = new String[]{"abcd3", "abcd3fghi301s"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testSpecialCharacterPasswords() {
        String[] list = new String[]{"$$$$$", "😀🤓👨‍💻"};
        for (String s : list) {
            assertFalse(pattern.matcher(s).matches());
        }
    }

    @Test
    public void testValidPasswords() {
        String[] list = new String[]{"AppLEs1", "HelloWorld1", "Password1"};
        for (String s : list) {
            assertTrue(pattern.matcher(s).matches());
        }
    }
}
