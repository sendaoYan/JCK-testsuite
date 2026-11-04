/*
 * @test
 * @summary java.awt.event.FocusAdapter conformance: white-box structure + black-box behavior.
 * @run main FocusAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.FocusAdapter;

public class FocusAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.FocusAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "FocusAdapter is public");
        check(Modifier.isAbstract(m), "FocusAdapter is abstract");
        check(c.getSuperclass() == Object.class, "FocusAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.FocusListener.class), "implements java.awt.event.FocusListener");
        check(c.getDeclaredMethods().length == 2, "FocusAdapter has 2 no-op methods");
        Method m0 = c.getMethod("focusGained", java.awt.event.FocusEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "focusGained public");
        Method m1 = c.getMethod("focusLost", java.awt.event.FocusEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "focusLost public");
        // ctor visibility (version-differentiated: JDK-8 public, JDK-9+ protected)
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "FocusAdapter ctor is protected");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("focusGained", java.awt.event.FocusEvent.class).getModifiers()), "focusGained public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("focusLost", java.awt.event.FocusEvent.class).getModifiers()), "focusLost public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        FocusAdapter a = new FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { calls.add("focusGained"); }
            // focusLost(java.awt.event.FocusEvent) inherited as no-op
        };
        a.focusGained(null); // no-op default method must not throw
        a.focusLost(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("focusGained"), "only overridden method dispatched");
        check(a instanceof java.awt.event.FocusListener, "FocusAdapter instanceof java.awt.event.FocusListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
