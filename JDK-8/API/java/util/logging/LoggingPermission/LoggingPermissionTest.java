/*
 * @test
 * @summary java.util.logging.LoggingPermission conformance: name/actions/equals.
 * @run main LoggingPermissionTest
 */

import java.security.BasicPermission;
import java.util.logging.LoggingPermission;

public class LoggingPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLoggingPermission() {
        LoggingPermission p = new LoggingPermission("control", null);
        check("control".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new LoggingPermission("control", null)), "equals");
    }

    public static void main(String[] args) {
        testLoggingPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
