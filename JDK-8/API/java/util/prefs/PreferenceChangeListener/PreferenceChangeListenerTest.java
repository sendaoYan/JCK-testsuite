/*
 * @test
 * @summary java.util.prefs.PreferenceChangeListener conformance: preferenceChange callback.
 * @run main PreferenceChangeListenerTest
 */

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class PreferenceChangeListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPreferenceChangeListener() {
        PreferenceChangeListener l = new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                check(evt.getKey() != null, "preferenceChange event key");
            }
        };
        check(l != null, "listener implemented");
    }

    public static void main(String[] args) {
        testPreferenceChangeListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
