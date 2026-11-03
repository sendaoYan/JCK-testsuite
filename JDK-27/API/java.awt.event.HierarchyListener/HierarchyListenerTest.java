/*
 * @test
 * @summary java.awt.event.HierarchyListener conformance: white-box structure + black-box behavior.
 * @run main HierarchyListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.HierarchyListener;

public class HierarchyListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.HierarchyListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "HierarchyListener is public");
        check(c.isInterface(), "HierarchyListener is interface");
        check(c.getSuperclass() == null, "HierarchyListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "HierarchyListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "HierarchyListener has 1 methods");
        Method m0 = c.getMethod("hierarchyChanged", java.awt.event.HierarchyEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "hierarchyChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        HierarchyListener l = new HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) { calls.add("hierarchyChanged"); }
        };
        l.hierarchyChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("hierarchyChanged"), "dispatch hierarchyChanged");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        HierarchyListener lambda = (e) -> lam.add("x");
        lambda.hierarchyChanged(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "HierarchyListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
