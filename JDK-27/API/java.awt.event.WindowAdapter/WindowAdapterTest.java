/*
 * @test
 * @summary java.awt.event.WindowAdapter conformance: white-box structure + black-box behavior.
 * @run main WindowAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.WindowAdapter;

public class WindowAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.WindowAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "WindowAdapter is public");
        check(Modifier.isAbstract(m), "WindowAdapter is abstract");
        check(c.getSuperclass() == Object.class, "WindowAdapter extends Object");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.WindowListener.class), "implements java.awt.event.WindowListener");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.WindowStateListener.class), "implements java.awt.event.WindowStateListener");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.awt.event.WindowFocusListener.class), "implements java.awt.event.WindowFocusListener");
        check(c.getDeclaredMethods().length == 10, "WindowAdapter has 10 no-op methods");
        Method m0 = c.getMethod("windowOpened", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m0.getModifiers()), "windowOpened public");
        Method m1 = c.getMethod("windowClosing", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m1.getModifiers()), "windowClosing public");
        Method m2 = c.getMethod("windowClosed", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m2.getModifiers()), "windowClosed public");
        Method m3 = c.getMethod("windowIconified", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m3.getModifiers()), "windowIconified public");
        Method m4 = c.getMethod("windowDeiconified", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m4.getModifiers()), "windowDeiconified public");
        Method m5 = c.getMethod("windowActivated", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m5.getModifiers()), "windowActivated public");
        Method m6 = c.getMethod("windowDeactivated", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m6.getModifiers()), "windowDeactivated public");
        Method m7 = c.getMethod("windowStateChanged", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m7.getModifiers()), "windowStateChanged public");
        Method m8 = c.getMethod("windowGainedFocus", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m8.getModifiers()), "windowGainedFocus public");
        Method m9 = c.getMethod("windowLostFocus", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m9.getModifiers()), "windowLostFocus public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        WindowAdapter a = new WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) { calls.add("windowOpened"); }
            // windowClosing(java.awt.event.WindowEvent) inherited as no-op
            // windowClosed(java.awt.event.WindowEvent) inherited as no-op
            // windowIconified(java.awt.event.WindowEvent) inherited as no-op
            // windowDeiconified(java.awt.event.WindowEvent) inherited as no-op
            // windowActivated(java.awt.event.WindowEvent) inherited as no-op
            // windowDeactivated(java.awt.event.WindowEvent) inherited as no-op
            // windowStateChanged(java.awt.event.WindowEvent) inherited as no-op
            // windowGainedFocus(java.awt.event.WindowEvent) inherited as no-op
            // windowLostFocus(java.awt.event.WindowEvent) inherited as no-op
        };
        a.windowOpened(null); // no-op default method must not throw
        a.windowClosing(null); // no-op default method must not throw
        a.windowClosed(null); // no-op default method must not throw
        a.windowIconified(null); // no-op default method must not throw
        a.windowDeiconified(null); // no-op default method must not throw
        a.windowActivated(null); // no-op default method must not throw
        a.windowDeactivated(null); // no-op default method must not throw
        a.windowStateChanged(null); // no-op default method must not throw
        a.windowGainedFocus(null); // no-op default method must not throw
        a.windowLostFocus(null); // no-op default method must not throw
        check(calls.size() == 1 && calls.get(0).equals("windowOpened"), "only overridden method dispatched");
        check(a instanceof java.awt.event.WindowListener, "WindowAdapter instanceof java.awt.event.WindowListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
