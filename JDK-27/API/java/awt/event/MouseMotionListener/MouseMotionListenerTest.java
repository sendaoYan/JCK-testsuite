/*
 * @test
 * @summary java.awt.event.MouseMotionListener conformance: white-box structure + black-box behavior.
 * @run main MouseMotionListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.MouseMotionListener;

public class MouseMotionListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseMotionListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseMotionListener is public");
        check(c.isInterface(), "MouseMotionListener is interface");
        check(c.getSuperclass() == null, "MouseMotionListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "MouseMotionListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "MouseMotionListener has 2 methods");
        Method m0 = c.getMethod("mouseDragged", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "mouseDragged public abstract");
        Method m1 = c.getMethod("mouseMoved", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "mouseMoved public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        MouseMotionListener l = new MouseMotionListener() {
            public void mouseDragged(java.awt.event.MouseEvent e) { calls.add("mouseDragged"); }
            public void mouseMoved(java.awt.event.MouseEvent e) { calls.add("mouseMoved"); }
        };
        l.mouseDragged(null);
        check(calls.size() == 1 && calls.get(0).equals("mouseDragged"), "dispatch mouseDragged");
        l.mouseMoved(null);
        check(calls.size() == 2 && calls.get(1).equals("mouseMoved"), "dispatch mouseMoved");
        check(l instanceof java.util.EventListener, "MouseMotionListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
