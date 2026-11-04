/*
 * @test
 * @summary java.lang.management.ManagementPermission conformance: white-box structure + black-box behavior.
 * @run main ManagementPermissionTest
 */
import java.lang.management.ManagementPermission;
import java.lang.reflect.Modifier;
import java.security.BasicPermission;

public class ManagementPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.ManagementPermission");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "constructor (String)");
        check(c.getConstructor(String.class, String.class) != null, "constructor (String,String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ManagementPermission control = new ManagementPermission("control");
        check("control".equals(control.getName()), "getName control");
        check("".equals(control.getActions()), "no-arg constructor empty actions");

        ManagementPermission monitor = new ManagementPermission("monitor", null);
        check("monitor".equals(monitor.getName()), "getName monitor");
        check("".equals(monitor.getActions()), "null actions empty");

        ManagementPermission monitor2 = new ManagementPermission("monitor", "");
        check("".equals(monitor2.getActions()), "empty actions");
        check(monitor.implies(monitor2), "implies same name");

        try {
            new ManagementPermission("control", "read");
            check(false, "non-empty actions should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "non-empty actions throws IllegalArgumentException");
        }

        try {
            new ManagementPermission("jck.bogus.Name.WithDot");
            check(false, "dotted name should be rejected");
        } catch (IllegalArgumentException e) {
            check(true, "dotted name throws IllegalArgumentException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
