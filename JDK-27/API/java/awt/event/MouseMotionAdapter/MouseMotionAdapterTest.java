/*
 * @test
 * @summary java.awt.event.MouseMotionAdapter conformance: white-box structure + black-box behavior.
 * @run main MouseMotionAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.MouseMotionAdapter;

public class MouseMotionAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseMotionAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseMotionAdapter is public");
        check(Modifier.isAbstract(m), "MouseMotionAdapter is abstract");
        check(c.getSuperclass() == Object.class, "MouseMotionAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.MouseMotionListener.class), "implements java.awt.event.MouseMotionListener");
        check(c.getDeclaredMethods().length == 2, "MouseMotionAdapter has 2 no-op methods");
        Method m0 = c.getMethod("mouseDragged", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "mouseDragged public");
        Method m1 = c.getMethod("mouseMoved", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "mouseMoved public");
        // ctor visibility (version-differentiated: JDK-8 public, JDK-9+ protected)
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "MouseMotionAdapter ctor is protected");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseDragged", java.awt.event.MouseEvent.class).getModifiers()), "mouseDragged public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseMoved", java.awt.event.MouseEvent.class).getModifiers()), "mouseMoved public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        MouseMotionAdapter a = new MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) { calls.add("mouseDragged"); }
            // mouseMoved(java.awt.event.MouseEvent) inherited as no-op
        };
        a.mouseDragged(null); // no-op default method must not throw
        a.mouseMoved(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("mouseDragged"), "only overridden method dispatched");
        check(a instanceof java.awt.event.MouseMotionListener, "MouseMotionAdapter instanceof java.awt.event.MouseMotionListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
