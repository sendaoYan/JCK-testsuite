/*
 * @test
 * @summary java.awt.event.ContainerListener conformance: white-box structure + black-box behavior.
 * @run main ContainerListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ContainerListener;

public class ContainerListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ContainerListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ContainerListener is public");
        check(c.isInterface(), "ContainerListener is interface");
        check(c.getSuperclass() == null, "ContainerListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "ContainerListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "ContainerListener has 2 methods");
        Method m0 = c.getMethod("componentAdded", java.awt.event.ContainerEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "componentAdded public abstract");
        Method m1 = c.getMethod("componentRemoved", java.awt.event.ContainerEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "componentRemoved public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ContainerListener l = new ContainerListener() {
            public void componentAdded(java.awt.event.ContainerEvent e) { calls.add("componentAdded"); }
            public void componentRemoved(java.awt.event.ContainerEvent e) { calls.add("componentRemoved"); }
        };
        l.componentAdded(null);
        check(calls.size() == 1 && calls.get(0).equals("componentAdded"), "dispatch componentAdded");
        l.componentRemoved(null);
        check(calls.size() == 2 && calls.get(1).equals("componentRemoved"), "dispatch componentRemoved");
        check(l instanceof java.util.EventListener, "ContainerListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
