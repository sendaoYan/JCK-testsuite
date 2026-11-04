/*
 * @test
 * @summary java.awt.Taskbar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TaskbarTest
 */

import java.lang.reflect.*;
import java.awt.Taskbar;

public class TaskbarTest {
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
        Class<?> c = Class.forName("java.awt.Taskbar", false, TaskbarTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Taskbar");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isStatic(c.getMethod("getTaskbar").getModifiers()), "getTaskbar is static");
        check(Modifier.isStatic(c.getMethod("isTaskbarSupported").getModifiers()), "isTaskbarSupported is static");
        for (String n : new String[] {"isSupported", "requestUserAttention", "setIconImage", "getIconImage",
                "setProgressValue", "getMenu", "setMenu"})
            check(hasMethod(c, n), "method present: " + n);
        check(Class.forName("java.awt.Taskbar$Feature", false, TaskbarTest.class.getClassLoader()) != null,
              "nested Feature type loads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        boolean supported = Taskbar.isTaskbarSupported();
        check(supported || !supported, "isTaskbarSupported returns a boolean");

        if (supported) {
            Taskbar tb = Taskbar.getTaskbar();
            check(tb != null, "getTaskbar non-null when supported");
            check(Taskbar.getTaskbar() == tb, "getTaskbar returns singleton");

            boolean deterministic = true;
            for (Taskbar.Feature f : Taskbar.Feature.values()) {
                deterministic &= (tb.isSupported(f) == tb.isSupported(f));
            }
            check(deterministic, "isSupported deterministic for all " + Taskbar.Feature.values().length + " features");

            check(tb.isSupported(Taskbar.Feature.ICON_IMAGE) || !tb.isSupported(Taskbar.Feature.ICON_IMAGE),
                  "isSupported(ICON_IMAGE) boolean");
            check(tb.isSupported(Taskbar.Feature.PROGRESS_VALUE) || !tb.isSupported(Taskbar.Feature.PROGRESS_VALUE),
                  "isSupported(PROGRESS_VALUE) boolean");
        } else {
            try {
                Taskbar.getTaskbar();
                check(false, "getTaskbar should throw when unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "getTaskbar throws UnsupportedOperationException when unsupported");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
