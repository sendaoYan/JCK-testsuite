/*
 * @test
 * @summary java.awt.event.KeyAdapter conformance: white-box structure + black-box behavior.
 * @run main KeyAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.KeyAdapter;

public class KeyAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.KeyAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "KeyAdapter is public");
        check(Modifier.isAbstract(m), "KeyAdapter is abstract");
        check(c.getSuperclass() == Object.class, "KeyAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.KeyListener.class), "implements java.awt.event.KeyListener");
        check(c.getDeclaredMethods().length == 3, "KeyAdapter has 3 no-op methods");
        Method m0 = c.getMethod("keyTyped", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "keyTyped public");
        Method m1 = c.getMethod("keyPressed", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "keyPressed public");
        Method m2 = c.getMethod("keyReleased", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m2.getModifiers()), "keyReleased public");
        // ctor visibility (version-differentiated: JDK-8 public, JDK-9+ protected)
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "KeyAdapter ctor is protected");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("keyTyped", java.awt.event.KeyEvent.class).getModifiers()), "keyTyped public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("keyPressed", java.awt.event.KeyEvent.class).getModifiers()), "keyPressed public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("keyReleased", java.awt.event.KeyEvent.class).getModifiers()), "keyReleased public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        KeyAdapter a = new KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) { calls.add("keyTyped"); }
            // keyPressed(java.awt.event.KeyEvent) inherited as no-op
            // keyReleased(java.awt.event.KeyEvent) inherited as no-op
        };
        a.keyTyped(null); // no-op default method must not throw
        a.keyPressed(null); // no-op default method must not throw
        a.keyReleased(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("keyTyped"), "only overridden method dispatched");
        check(a instanceof java.awt.event.KeyListener, "KeyAdapter instanceof java.awt.event.KeyListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
