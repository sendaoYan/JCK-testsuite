/*
 * @test
 * @summary java.awt.DefaultKeyboardFocusManager conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DefaultKeyboardFocusManagerTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class DefaultKeyboardFocusManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.DefaultKeyboardFocusManager");
        check(c != null, "type loads: java.awt.DefaultKeyboardFocusManager");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == KeyboardFocusManager.class, "extends KeyboardFocusManager");
        for (String name : new String[]{"dispatchEvent", "dispatchKeyEvent", "postProcessKeyEvent",
                "processKeyEvent", "focusNextComponent", "focusPreviousComponent",
                "upFocusCycle", "downFocusCycle"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultKeyboardFocusManager d = new DefaultKeyboardFocusManager();
        check(d instanceof KeyboardFocusManager, "is a KeyboardFocusManager");

        FocusTraversalPolicy ftp = d.getDefaultFocusTraversalPolicy();
        check(ftp != null, "default focus traversal policy non-null");

        java.util.Set<AWTKeyStroke> fwd = d.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        check(fwd != null && !fwd.isEmpty(), "forward traversal keys non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
