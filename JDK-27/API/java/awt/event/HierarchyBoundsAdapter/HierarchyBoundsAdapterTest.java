/*
 * @test
 * @summary java.awt.event.HierarchyBoundsAdapter conformance: white-box structure + black-box behavior.
 * @run main HierarchyBoundsAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.HierarchyBoundsAdapter;

public class HierarchyBoundsAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.HierarchyBoundsAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "HierarchyBoundsAdapter is public");
        check(Modifier.isAbstract(m), "HierarchyBoundsAdapter is abstract");
        check(c.getSuperclass() == Object.class, "HierarchyBoundsAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.HierarchyBoundsListener.class), "implements java.awt.event.HierarchyBoundsListener");
        check(c.getDeclaredMethods().length == 2, "HierarchyBoundsAdapter has 2 no-op methods");
        Method m0 = c.getMethod("ancestorMoved", java.awt.event.HierarchyEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "ancestorMoved public");
        Method m1 = c.getMethod("ancestorResized", java.awt.event.HierarchyEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "ancestorResized public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        HierarchyBoundsAdapter a = new HierarchyBoundsAdapter() {
            public void ancestorMoved(java.awt.event.HierarchyEvent e) { calls.add("ancestorMoved"); }
            // ancestorResized(java.awt.event.HierarchyEvent) inherited as no-op
        };
        a.ancestorMoved(null); // no-op default method must not throw
        a.ancestorResized(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("ancestorMoved"), "only overridden method dispatched");
        check(a instanceof java.awt.event.HierarchyBoundsListener, "HierarchyBoundsAdapter instanceof java.awt.event.HierarchyBoundsListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
