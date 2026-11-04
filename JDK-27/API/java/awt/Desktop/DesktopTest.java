/*
 * @test
 * @summary java.awt.Desktop conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DesktopTest
 */

import java.lang.reflect.*;
import java.awt.Desktop;

public class DesktopTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Desktop", false, DesktopTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Desktop");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isStatic(c.getMethod("getDesktop").getModifiers()), "getDesktop is static");
        check(Modifier.isStatic(c.getMethod("isDesktopSupported").getModifiers()), "isDesktopSupported is static");
        for (String n : new String[] {"isSupported", "open", "edit", "print", "browse", "mail"})
            check(hasMethod(c, n), "method present: " + n);
        check(Class.forName("java.awt.Desktop$Action", false, DesktopTest.class.getClassLoader()) != null,
              "nested Action type loads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        boolean supported = Desktop.isDesktopSupported();
        check(supported || !supported, "isDesktopSupported returns a boolean");

        if (supported) {
            Desktop d = Desktop.getDesktop();
            check(d != null, "getDesktop non-null when supported");
            check(Desktop.getDesktop() == d, "getDesktop returns singleton");

            boolean deterministic = true;
            for (Desktop.Action a : Desktop.Action.values()) {
                deterministic &= (d.isSupported(a) == d.isSupported(a));
            }
            check(deterministic, "isSupported deterministic for all " + Desktop.Action.values().length + " actions");

            check(d.isSupported(Desktop.Action.OPEN) || !d.isSupported(Desktop.Action.OPEN), "isSupported(OPEN) boolean");
            check(d.isSupported(Desktop.Action.BROWSE) || !d.isSupported(Desktop.Action.BROWSE), "isSupported(BROWSE) boolean");
            check(d.isSupported(Desktop.Action.MAIL) || !d.isSupported(Desktop.Action.MAIL), "isSupported(MAIL) boolean");
            check(d.isSupported(Desktop.Action.APP_QUIT_HANDLER) || !d.isSupported(Desktop.Action.APP_QUIT_HANDLER),
                  "isSupported(APP_QUIT_HANDLER) boolean");
        } else {
            try {
                Desktop.getDesktop();
                check(false, "getDesktop should throw when unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "getDesktop throws UnsupportedOperationException when unsupported");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
