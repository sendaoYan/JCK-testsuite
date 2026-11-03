/*
 * @test
 * @summary java.awt.event.HierarchyBoundsListener conformance: white-box structure + black-box behavior.
 * @run main HierarchyBoundsListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.HierarchyBoundsListener;

public class HierarchyBoundsListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.HierarchyBoundsListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "HierarchyBoundsListener is public");
        check(c.isInterface(), "HierarchyBoundsListener is interface");
        check(c.getSuperclass() == null, "HierarchyBoundsListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "HierarchyBoundsListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "HierarchyBoundsListener has 2 methods");
        Method m0 = c.getMethod("ancestorMoved", java.awt.event.HierarchyEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "ancestorMoved public abstract");
        Method m1 = c.getMethod("ancestorResized", java.awt.event.HierarchyEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "ancestorResized public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        HierarchyBoundsListener l = new HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent e) { calls.add("ancestorMoved"); }
            public void ancestorResized(java.awt.event.HierarchyEvent e) { calls.add("ancestorResized"); }
        };
        l.ancestorMoved(null);
        check(calls.size() == 1 && calls.get(0).equals("ancestorMoved"), "dispatch ancestorMoved");
        l.ancestorResized(null);
        check(calls.size() == 2 && calls.get(1).equals("ancestorResized"), "dispatch ancestorResized");
        check(l instanceof java.util.EventListener, "HierarchyBoundsListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
