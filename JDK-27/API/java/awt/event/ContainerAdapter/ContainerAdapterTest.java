/*
 * @test
 * @summary java.awt.event.ContainerAdapter conformance: white-box structure + black-box behavior.
 * @run main ContainerAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ContainerAdapter;

public class ContainerAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ContainerAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ContainerAdapter is public");
        check(Modifier.isAbstract(m), "ContainerAdapter is abstract");
        check(c.getSuperclass() == Object.class, "ContainerAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.ContainerListener.class), "implements java.awt.event.ContainerListener");
        check(c.getDeclaredMethods().length == 2, "ContainerAdapter has 2 no-op methods");
        Method m0 = c.getMethod("componentAdded", java.awt.event.ContainerEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "componentAdded public");
        Method m1 = c.getMethod("componentRemoved", java.awt.event.ContainerEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "componentRemoved public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ContainerAdapter a = new ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent e) { calls.add("componentAdded"); }
            // componentRemoved(java.awt.event.ContainerEvent) inherited as no-op
        };
        a.componentAdded(null); // no-op default method must not throw
        a.componentRemoved(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("componentAdded"), "only overridden method dispatched");
        check(a instanceof java.awt.event.ContainerListener, "ContainerAdapter instanceof java.awt.event.ContainerListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
