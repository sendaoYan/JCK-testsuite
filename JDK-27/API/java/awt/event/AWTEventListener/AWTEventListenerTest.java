/*
 * @test
 * @summary java.awt.event.AWTEventListener conformance: white-box structure + black-box behavior.
 * @run main AWTEventListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.AWTEventListener;

public class AWTEventListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.AWTEventListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AWTEventListener is public");
        check(c.isInterface(), "AWTEventListener is interface");
        check(c.getSuperclass() == null, "AWTEventListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "AWTEventListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "AWTEventListener has 1 methods");
        Method m0 = c.getMethod("eventDispatched", java.awt.AWTEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "eventDispatched public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        AWTEventListener l = new AWTEventListener() {
            public void eventDispatched(java.awt.AWTEvent e) { calls.add("eventDispatched"); }
        };
        l.eventDispatched(null);
        check(calls.size() == 1 && calls.get(0).equals("eventDispatched"), "dispatch eventDispatched");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        AWTEventListener lambda = (e) -> lam.add("x");
        lambda.eventDispatched(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "AWTEventListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
