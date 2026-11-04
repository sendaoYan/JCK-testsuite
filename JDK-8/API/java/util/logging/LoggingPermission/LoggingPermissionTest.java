/*
 * @test
 * @summary java.util.logging.LoggingPermission conformance: white-box structure + black-box behavior.
 * @run main LoggingPermissionTest
 */

import java.lang.reflect.Modifier;
import java.security.BasicPermission;
import java.security.Permission;
import java.util.logging.LoggingPermission;

public class LoggingPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LoggingPermission");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(Permission.class.isAssignableFrom(c), "is a Permission");
        check(c.getSuperclass() == BasicPermission.class, "direct superclass BasicPermission");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) constructor");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LoggingPermission p = new LoggingPermission("control", null);
        check("control".equals(p.getName()), "getName control");
        check("".equals(p.getActions()), "null actions -> empty string");

        LoggingPermission p2 = new LoggingPermission("control", "");
        check("".equals(p2.getActions()), "empty actions preserved");

        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(p2), "equal by name+class");
        check(p.hashCode() == p2.hashCode(), "equal permissions equal hashCode");
        check(p.equals(new LoggingPermission("control", null)), "equals same name");
        check(!p.equals(null), "equals null false");
        check(!p.equals("control"), "equals non-permission false");

        check(p.implies(new LoggingPermission("control", null)), "implies same name");
        check(p.toString().contains("control"), "toString contains name");

        // only the name "control" is valid; non-empty actions are rejected
        expectThrows(IllegalArgumentException.class,
                () -> new LoggingPermission("other", null), "name != control IAE");
        expectThrows(IllegalArgumentException.class,
                () -> new LoggingPermission("control", "read"), "non-empty actions IAE");
        expectThrows(NullPointerException.class,
                () -> new LoggingPermission(null, null), "null name NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
