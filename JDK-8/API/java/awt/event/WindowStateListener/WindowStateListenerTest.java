/*
 * @test
 * @summary java.awt.event.WindowStateListener conformance: white-box structure + black-box behavior.
 * @run main WindowStateListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.WindowStateListener;

public class WindowStateListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.WindowStateListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "WindowStateListener is public");
        check(c.isInterface(), "WindowStateListener is interface");
        check(c.getSuperclass() == null, "WindowStateListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "WindowStateListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "WindowStateListener has 1 methods");
        Method m0 = c.getMethod("windowStateChanged", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "windowStateChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        WindowStateListener l = new WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent e) { calls.add("windowStateChanged"); }
        };
        l.windowStateChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("windowStateChanged"), "dispatch windowStateChanged");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        WindowStateListener lambda = (e) -> lam.add("x");
        lambda.windowStateChanged(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "WindowStateListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
