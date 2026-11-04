/*
 * @test
 * @summary java.awt.event.ComponentAdapter conformance: white-box structure + black-box behavior.
 * @run main ComponentAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ComponentAdapter;

public class ComponentAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ComponentAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ComponentAdapter is public");
        check(Modifier.isAbstract(m), "ComponentAdapter is abstract");
        check(c.getSuperclass() == Object.class, "ComponentAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.ComponentListener.class), "implements java.awt.event.ComponentListener");
        check(c.getDeclaredMethods().length == 4, "ComponentAdapter has 4 no-op methods");
        Method m0 = c.getMethod("componentResized", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "componentResized public");
        Method m1 = c.getMethod("componentMoved", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "componentMoved public");
        Method m2 = c.getMethod("componentShown", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m2.getModifiers()), "componentShown public");
        Method m3 = c.getMethod("componentHidden", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m3.getModifiers()), "componentHidden public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ComponentAdapter a = new ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) { calls.add("componentResized"); }
            // componentMoved(java.awt.event.ComponentEvent) inherited as no-op
            // componentShown(java.awt.event.ComponentEvent) inherited as no-op
            // componentHidden(java.awt.event.ComponentEvent) inherited as no-op
        };
        a.componentResized(null); // no-op default method must not throw
        a.componentMoved(null); // no-op default method must not throw
        a.componentShown(null); // no-op default method must not throw
        a.componentHidden(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("componentResized"), "only overridden method dispatched");
        check(a instanceof java.awt.event.ComponentListener, "ComponentAdapter instanceof java.awt.event.ComponentListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
