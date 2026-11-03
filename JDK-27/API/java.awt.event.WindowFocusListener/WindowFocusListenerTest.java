/*
 * @test
 * @summary java.awt.event.WindowFocusListener conformance: white-box structure + black-box behavior.
 * @run main WindowFocusListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.WindowFocusListener;

public class WindowFocusListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.WindowFocusListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "WindowFocusListener is public");
        check(c.isInterface(), "WindowFocusListener is interface");
        check(c.getSuperclass() == null, "WindowFocusListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "WindowFocusListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "WindowFocusListener has 2 methods");
        Method m0 = c.getMethod("windowGainedFocus", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "windowGainedFocus public abstract");
        Method m1 = c.getMethod("windowLostFocus", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "windowLostFocus public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        WindowFocusListener l = new WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) { calls.add("windowGainedFocus"); }
            public void windowLostFocus(java.awt.event.WindowEvent e) { calls.add("windowLostFocus"); }
        };
        l.windowGainedFocus(null);
        check(calls.size() == 1 && calls.get(0).equals("windowGainedFocus"), "dispatch windowGainedFocus");
        l.windowLostFocus(null);
        check(calls.size() == 2 && calls.get(1).equals("windowLostFocus"), "dispatch windowLostFocus");
        check(l instanceof java.util.EventListener, "WindowFocusListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
