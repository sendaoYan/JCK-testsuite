/*
 * @test
 * @summary java.awt.KeyboardFocusManager conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main KeyboardFocusManagerTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class KeyboardFocusManagerTest {
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
        Class<?> c = Class.forName("java.awt.KeyboardFocusManager");
        check(c != null, "type loads: java.awt.KeyboardFocusManager");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract");
        boolean dispatcher = false, postProcessor = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == KeyEventDispatcher.class) dispatcher = true;
            if (i == KeyEventPostProcessor.class) postProcessor = true;
        }
        check(dispatcher, "implements KeyEventDispatcher");
        check(postProcessor, "implements KeyEventPostProcessor");
        for (String name : new String[]{"getCurrentKeyboardFocusManager", "setCurrentKeyboardFocusManager",
                "getDefaultFocusTraversalPolicy", "setDefaultFocusTraversalPolicy",
                "getDefaultFocusTraversalKeys", "setDefaultFocusTraversalKeys",
                "getFocusOwner", "getFocusedWindow", "getActiveWindow", "addKeyEventDispatcher",
                "removeKeyEventDispatcher", "focusNextComponent", "focusPreviousComponent"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS == 0, "FORWARD_TRAVERSAL_KEYS 0");
        check(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS == 1, "BACKWARD_TRAVERSAL_KEYS 1");
        check(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS == 2, "UP_CYCLE_TRAVERSAL_KEYS 2");
        check(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS == 3, "DOWN_CYCLE_TRAVERSAL_KEYS 3");

        KeyboardFocusManager km = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        check(km != null, "current manager non-null");
        check(km instanceof KeyboardFocusManager, "current manager type");

        FocusTraversalPolicy ftp = km.getDefaultFocusTraversalPolicy();
        check(ftp != null, "default focus traversal policy non-null");

        java.util.Set<AWTKeyStroke> fwd = km.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        java.util.Set<AWTKeyStroke> bwd = km.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        check(fwd != null && bwd != null, "default traversal key sets non-null");
        check(!fwd.isEmpty() && !bwd.isEmpty(), "default traversal key sets non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
