/*
 * @test
 * @summary java.util.prefs.PreferenceChangeEvent conformance: getNode/getKey/getNewValue.
 * @run main PreferenceChangeEventTest
 */

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

public class PreferenceChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPreferenceChangeEvent() {
        Preferences node = Preferences.userRoot();
        PreferenceChangeEvent e = new PreferenceChangeEvent(node, "key", "newValue");
        check(e.getNode() == node, "getNode");
        check("key".equals(e.getKey()), "getKey");
        check("newValue".equals(e.getNewValue()), "getNewValue");
        check(e instanceof java.util.EventObject, "is EventObject");
    }

    public static void main(String[] args) {
        testPreferenceChangeEvent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
