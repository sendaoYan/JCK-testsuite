/*
 * @test
 * @summary java.util.prefs.PreferencesFactory conformance: userRoot/systemRoot contract.
 * @run main PreferencesFactoryTest
 */

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class PreferencesFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPreferencesFactory() {
        PreferencesFactory factory = new PreferencesFactory() {
            public Preferences systemRoot() {
                return Preferences.systemRoot();
            }

            public Preferences userRoot() {
                return Preferences.userRoot();
            }
        };
        check(factory.userRoot().isUserNode(), "userRoot isUserNode");
        check(!factory.systemRoot().isUserNode(), "systemRoot not isUserNode");
    }

    public static void main(String[] args) {
        testPreferencesFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
