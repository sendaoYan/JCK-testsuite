/*
 * @test
 * @summary javax.security.auth.login.Configuration conformance: white-box structure + black-box behavior.
 * @run main ConfigurationTest
 */

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ConfigurationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expect(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " (got " + t.getClass().getName() + ")");
            return;
        }
        check(false, msg + " (no exception)");
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static boolean hasPublicCtor(Class<?> c) {
        return c.getConstructors().length > 0;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.Configuration");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends java.lang.Object");
        check(!hasPublicCtor(c), "no public constructor");
        for (String n : new String[]{"getAppConfigurationEntry","getProvider","getType","getParameters",
                                     "refresh","getConfiguration","setConfiguration","getInstance"})
            check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // capture original global configuration and restore in finally (isolation)
        Configuration original = Configuration.getConfiguration();
        check(original != null, "getConfiguration non-null");

        try {
            // default Configuration queries
            check(original.getParameters() == null, "default getParameters null");
            check(original.getType() == null, "default getType null");
            check(original.getProvider() == null, "default getProvider null");
            original.refresh(); // default no-op must not throw

            // setConfiguration/getConfiguration round-trip
            Configuration custom = new Configuration() {
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    if ("app".equals(name)) {
                        return new AppConfigurationEntry[] {
                            new AppConfigurationEntry("com.example.Mod",
                                LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>())
                        };
                    }
                    return null;
                }
            };
            Configuration.setConfiguration(custom);
            check(Configuration.getConfiguration() == custom, "set/get round-trip");
            AppConfigurationEntry[] entries = custom.getAppConfigurationEntry("app");
            check(entries != null && entries.length == 1, "custom getAppConfigurationEntry for app");
            check("com.example.Mod".equals(entries[0].getLoginModuleName()), "entry module name");
            check(custom.getAppConfigurationEntry("missing") == null, "custom returns null for unknown name");

            // unknown configuration type is rejected deterministically
            expect(NoSuchAlgorithmException.class,
                    () -> Configuration.getInstance("JckNoSuchConfigType", null),
                    "getInstance unknown type throws NoSuchAlgorithmException");
        } finally {
            Configuration.setConfiguration(original);
        }

        check(Configuration.getConfiguration() == original, "original configuration restored");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
