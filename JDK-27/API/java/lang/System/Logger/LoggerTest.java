/*
 * @test
 * @summary java.lang.System.Logger conformance: white-box structure + black-box behavior.
 * @run main LoggerTest
 */

import java.lang.System;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

public class LoggerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(System.Logger.class.isInterface(), "System.Logger is an interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLogger() {
        System.Logger log = System.getLogger("logger-test");
        check("logger-test".equals(log.getName()), "getName");
        check(log.isLoggable(System.Logger.Level.INFO), "isLoggable INFO");
        log.log(System.Logger.Level.INFO, "msg");
        log.log(System.Logger.Level.INFO, () -> "supplier");
        log.log(System.Logger.Level.INFO, "obj-msg", new Object());
        log.log(System.Logger.Level.INFO, "msg-varargs", "a", "b");
        log.log(System.Logger.Level.WARNING, "warn", new Exception("x"));
        passed += 6;
    }

    static class Bundle extends ListResourceBundle {
        protected Object[][] getContents() { return new Object[][] {{"k", "v"}}; }
    }

    static void testResourceBundleLogger() {
        ResourceBundle bundle = new Bundle();
        System.Logger log = System.getLogger("logger-test-bundle", bundle);
        check(log != null, "getLogger(name, bundle) non-null");
        check("logger-test-bundle".equals(log.getName()), "bundle logger name");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testLogger();
        testResourceBundleLogger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
