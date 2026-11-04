/*
 * @test
 * @summary java.awt.event.ComponentListener conformance: white-box structure + black-box behavior.
 * @run main ComponentListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ComponentListener;

public class ComponentListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ComponentListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ComponentListener is public");
        check(c.isInterface(), "ComponentListener is interface");
        check(c.getSuperclass() == null, "ComponentListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "ComponentListener extends EventListener");
        check(c.getDeclaredMethods().length == 4, "ComponentListener has 4 methods");
        Method m0 = c.getMethod("componentResized", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "componentResized public abstract");
        Method m1 = c.getMethod("componentMoved", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "componentMoved public abstract");
        Method m2 = c.getMethod("componentShown", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m2.getModifiers()) && Modifier.isAbstract(m2.getModifiers()), "componentShown public abstract");
        Method m3 = c.getMethod("componentHidden", java.awt.event.ComponentEvent.class);
        check(Modifier.isPublic(m3.getModifiers()) && Modifier.isAbstract(m3.getModifiers()), "componentHidden public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ComponentListener l = new ComponentListener() {
            public void componentResized(java.awt.event.ComponentEvent e) { calls.add("componentResized"); }
            public void componentMoved(java.awt.event.ComponentEvent e) { calls.add("componentMoved"); }
            public void componentShown(java.awt.event.ComponentEvent e) { calls.add("componentShown"); }
            public void componentHidden(java.awt.event.ComponentEvent e) { calls.add("componentHidden"); }
        };
        l.componentResized(null);
        check(calls.size() == 1 && calls.get(0).equals("componentResized"), "dispatch componentResized");
        l.componentMoved(null);
        check(calls.size() == 2 && calls.get(1).equals("componentMoved"), "dispatch componentMoved");
        l.componentShown(null);
        check(calls.size() == 3 && calls.get(2).equals("componentShown"), "dispatch componentShown");
        l.componentHidden(null);
        check(calls.size() == 4 && calls.get(3).equals("componentHidden"), "dispatch componentHidden");
        check(l instanceof java.util.EventListener, "ComponentListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
