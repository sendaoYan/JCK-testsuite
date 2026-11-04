/*
 * @test
 * @summary java.awt.event.MouseAdapter conformance: white-box structure + black-box behavior.
 * @run main MouseAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.MouseAdapter;

public class MouseAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseAdapter is public");
        check(Modifier.isAbstract(m), "MouseAdapter is abstract");
        check(c.getSuperclass() == Object.class, "MouseAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.MouseListener.class), "implements java.awt.event.MouseListener");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.MouseWheelListener.class), "implements java.awt.event.MouseWheelListener");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.MouseMotionListener.class), "implements java.awt.event.MouseMotionListener");
        check(c.getDeclaredMethods().length == 8, "MouseAdapter has 8 no-op methods");
        Method m0 = c.getMethod("mouseClicked", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "mouseClicked public");
        Method m1 = c.getMethod("mousePressed", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "mousePressed public");
        Method m2 = c.getMethod("mouseReleased", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m2.getModifiers()), "mouseReleased public");
        Method m3 = c.getMethod("mouseEntered", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m3.getModifiers()), "mouseEntered public");
        Method m4 = c.getMethod("mouseExited", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m4.getModifiers()), "mouseExited public");
        Method m5 = c.getMethod("mouseWheelMoved", java.awt.event.MouseWheelEvent.class);
        check(Modifier.isPublic(m5.getModifiers()), "mouseWheelMoved public");
        Method m6 = c.getMethod("mouseDragged", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m6.getModifiers()), "mouseDragged public");
        Method m7 = c.getMethod("mouseMoved", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m7.getModifiers()), "mouseMoved public");
        // ctor visibility (version-differentiated: JDK-8 public, JDK-9+ protected)
        check(java.lang.reflect.Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "MouseAdapter ctor is public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseClicked", java.awt.event.MouseEvent.class).getModifiers()), "mouseClicked public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mousePressed", java.awt.event.MouseEvent.class).getModifiers()), "mousePressed public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseReleased", java.awt.event.MouseEvent.class).getModifiers()), "mouseReleased public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseEntered", java.awt.event.MouseEvent.class).getModifiers()), "mouseEntered public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseExited", java.awt.event.MouseEvent.class).getModifiers()), "mouseExited public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseWheelMoved", java.awt.event.MouseWheelEvent.class).getModifiers()), "mouseWheelMoved public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseDragged", java.awt.event.MouseEvent.class).getModifiers()), "mouseDragged public");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("mouseMoved", java.awt.event.MouseEvent.class).getModifiers()), "mouseMoved public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        MouseAdapter a = new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { calls.add("mouseClicked"); }
            // mousePressed(java.awt.event.MouseEvent) inherited as no-op
            // mouseReleased(java.awt.event.MouseEvent) inherited as no-op
            // mouseEntered(java.awt.event.MouseEvent) inherited as no-op
            // mouseExited(java.awt.event.MouseEvent) inherited as no-op
            // mouseWheelMoved(java.awt.event.MouseWheelEvent) inherited as no-op
            // mouseDragged(java.awt.event.MouseEvent) inherited as no-op
            // mouseMoved(java.awt.event.MouseEvent) inherited as no-op
        };
        a.mouseClicked(null); // no-op default method must not throw
        a.mousePressed(null); // no-op default method must not throw
        a.mouseReleased(null); // no-op default method must not throw
        a.mouseEntered(null); // no-op default method must not throw
        a.mouseExited(null); // no-op default method must not throw
        a.mouseWheelMoved(null); // no-op default method must not throw
        a.mouseDragged(null); // no-op default method must not throw
        a.mouseMoved(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("mouseClicked"), "only overridden method dispatched");
        check(a instanceof java.awt.event.MouseListener, "MouseAdapter instanceof java.awt.event.MouseListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
