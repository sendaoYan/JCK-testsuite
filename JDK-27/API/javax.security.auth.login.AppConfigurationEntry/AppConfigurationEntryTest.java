/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry conformance: white-box structure + black-box behavior.
 * @run main AppConfigurationEntryTest
 */

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import java.util.HashMap;
import java.util.Map;

public class AppConfigurationEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " (got " + t.getClass().getName() + ")");
            return;
        }
        check(false, msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.AppConfigurationEntry");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        c.getConstructor(String.class, LoginModuleControlFlag.class, Map.class);
        c.getMethod("getLoginModuleName");
        c.getMethod("getControlFlag");
        c.getMethod("getOptions");
        check(true, "ctor + 3 accessor methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("k", "v");
        AppConfigurationEntry e = new AppConfigurationEntry(
                "com.example.Module", LoginModuleControlFlag.REQUIRED, opts);
        check("com.example.Module".equals(e.getLoginModuleName()), "getLoginModuleName");
        check(e.getControlFlag() == LoginModuleControlFlag.REQUIRED, "getControlFlag identity");
        check("v".equals(e.getOptions().get("k")), "getOptions content");
        check(e.getOptions().size() == 1, "getOptions size");

        // getOptions returns an unmodifiable view
        @SuppressWarnings("unchecked")
        Map<String, Object> rawOpts = (Map<String, Object>) (Map) e.getOptions();
        expect(UnsupportedOperationException.class, () -> rawOpts.put("x", "y"),
                "getOptions().put throws UnsupportedOperationException");
        check(!e.getOptions().containsKey("x"), "options unchanged after failed put");

        // options is an unmodifiable view over the caller map (JDK behavior)
        opts.put("added", true);
        check(Boolean.TRUE.equals(e.getOptions().get("added")), "options is a live view of caller map");

        // all four control flags round-trip through the entry
        for (LoginModuleControlFlag f : new LoginModuleControlFlag[] {
                LoginModuleControlFlag.REQUIRED,
                LoginModuleControlFlag.REQUISITE,
                LoginModuleControlFlag.SUFFICIENT,
                LoginModuleControlFlag.OPTIONAL }) {
            AppConfigurationEntry a = new AppConfigurationEntry("m", f, new HashMap<String, Object>());
            check(a.getControlFlag() == f, "round-trip flag " + f);
        }

        // constructor validation
        expect(IllegalArgumentException.class,
                () -> new AppConfigurationEntry(null, LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>()),
                "null module name throws IllegalArgumentException");
        expect(IllegalArgumentException.class,
                () -> new AppConfigurationEntry("", LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>()),
                "empty module name throws IllegalArgumentException");
        expect(IllegalArgumentException.class,
                () -> new AppConfigurationEntry("m", null, new HashMap<String, Object>()),
                "null control flag throws IllegalArgumentException");
        expect(IllegalArgumentException.class,
                () -> new AppConfigurationEntry("m", LoginModuleControlFlag.REQUIRED, null),
                "null options throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
