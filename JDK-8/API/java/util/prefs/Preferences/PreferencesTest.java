/*
 * @test
 * @summary java.util.prefs.Preferences conformance: userRoot/systemRoot, node operations,
 *          size constants.
 * @run main PreferencesTest
 */

import java.util.prefs.Preferences;

public class PreferencesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPreferences() throws Exception {
        Preferences user = Preferences.userRoot();
        Preferences system = Preferences.systemRoot();
        check(user.isUserNode(), "userRoot isUserNode");
        check(!system.isUserNode(), "systemRoot not isUserNode");
        check(Preferences.MAX_KEY_LENGTH == 80, "MAX_KEY_LENGTH");
        check(Preferences.MAX_VALUE_LENGTH == 8192, "MAX_VALUE_LENGTH");
        check(Preferences.MAX_NAME_LENGTH == 80, "MAX_NAME_LENGTH");

        Preferences tmp = user.node("jck_prefs_test");
        try {
            tmp.put("key", "value");
            check("value".equals(tmp.get("key", null)), "put/get");
            tmp.putInt("int", 7);
            check(tmp.getInt("int", 0) == 7, "putInt/getInt");
            Preferences child = tmp.node("child");
            check(child.parent() == tmp, "parent");
            check("child".equals(child.name()), "name");
            check("/jck_prefs_test/child".equals(child.absolutePath()), "absolutePath");
            check(tmp.nodeExists("child"), "nodeExists");
            check(tmp.keys().length >= 2, "keys");
            check(tmp.childrenNames().length == 1, "childrenNames");
            tmp.flush();
            tmp.sync();
            check(tmp.toString() != null, "toString");
        } finally {
            tmp.removeNode();
        }
        check(!user.nodeExists("jck_prefs_test"), "removeNode");
    }

    public static void main(String[] args) throws Exception {
        testPreferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
