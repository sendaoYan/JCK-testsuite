/*
 * @test
 * @summary java.awt.event.MouseListener conformance: white-box structure + black-box behavior.
 * @run main MouseListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.MouseListener;

public class MouseListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseListener is public");
        check(c.isInterface(), "MouseListener is interface");
        check(c.getSuperclass() == null, "MouseListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "MouseListener extends EventListener");
        check(c.getDeclaredMethods().length == 5, "MouseListener has 5 methods");
        Method m0 = c.getMethod("mouseClicked", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "mouseClicked public abstract");
        Method m1 = c.getMethod("mousePressed", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "mousePressed public abstract");
        Method m2 = c.getMethod("mouseReleased", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m2.getModifiers()) && Modifier.isAbstract(m2.getModifiers()), "mouseReleased public abstract");
        Method m3 = c.getMethod("mouseEntered", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m3.getModifiers()) && Modifier.isAbstract(m3.getModifiers()), "mouseEntered public abstract");
        Method m4 = c.getMethod("mouseExited", java.awt.event.MouseEvent.class);
        check(Modifier.isPublic(m4.getModifiers()) && Modifier.isAbstract(m4.getModifiers()), "mouseExited public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        MouseListener l = new MouseListener() {
            public void mouseClicked(java.awt.event.MouseEvent e) { calls.add("mouseClicked"); }
            public void mousePressed(java.awt.event.MouseEvent e) { calls.add("mousePressed"); }
            public void mouseReleased(java.awt.event.MouseEvent e) { calls.add("mouseReleased"); }
            public void mouseEntered(java.awt.event.MouseEvent e) { calls.add("mouseEntered"); }
            public void mouseExited(java.awt.event.MouseEvent e) { calls.add("mouseExited"); }
        };
        l.mouseClicked(null);
        check(calls.size() == 1 && calls.get(0).equals("mouseClicked"), "dispatch mouseClicked");
        l.mousePressed(null);
        check(calls.size() == 2 && calls.get(1).equals("mousePressed"), "dispatch mousePressed");
        l.mouseReleased(null);
        check(calls.size() == 3 && calls.get(2).equals("mouseReleased"), "dispatch mouseReleased");
        l.mouseEntered(null);
        check(calls.size() == 4 && calls.get(3).equals("mouseEntered"), "dispatch mouseEntered");
        l.mouseExited(null);
        check(calls.size() == 5 && calls.get(4).equals("mouseExited"), "dispatch mouseExited");
        check(l instanceof java.util.EventListener, "MouseListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
