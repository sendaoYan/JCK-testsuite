/*
 * @test
 * @summary java.awt.event.MouseWheelListener conformance: white-box structure + black-box behavior.
 * @run main MouseWheelListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.MouseWheelListener;

public class MouseWheelListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseWheelListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseWheelListener is public");
        check(c.isInterface(), "MouseWheelListener is interface");
        check(c.getSuperclass() == null, "MouseWheelListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "MouseWheelListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "MouseWheelListener has 1 methods");
        Method m0 = c.getMethod("mouseWheelMoved", java.awt.event.MouseWheelEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "mouseWheelMoved public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        MouseWheelListener l = new MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) { calls.add("mouseWheelMoved"); }
        };
        l.mouseWheelMoved(null);
        check(calls.size() == 1 && calls.get(0).equals("mouseWheelMoved"), "dispatch mouseWheelMoved");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        MouseWheelListener lambda = (e) -> lam.add("x");
        lambda.mouseWheelMoved(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "MouseWheelListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
