/*
 * @test
 * @summary java.awt.event.WindowListener conformance: white-box structure + black-box behavior.
 * @run main WindowListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.WindowListener;

public class WindowListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.WindowListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "WindowListener is public");
        check(c.isInterface(), "WindowListener is interface");
        check(c.getSuperclass() == null, "WindowListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "WindowListener extends EventListener");
        check(c.getDeclaredMethods().length == 7, "WindowListener has 7 methods");
        Method m0 = c.getMethod("windowOpened", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "windowOpened public abstract");
        Method m1 = c.getMethod("windowClosing", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "windowClosing public abstract");
        Method m2 = c.getMethod("windowClosed", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m2.getModifiers()) && Modifier.isAbstract(m2.getModifiers()), "windowClosed public abstract");
        Method m3 = c.getMethod("windowIconified", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m3.getModifiers()) && Modifier.isAbstract(m3.getModifiers()), "windowIconified public abstract");
        Method m4 = c.getMethod("windowDeiconified", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m4.getModifiers()) && Modifier.isAbstract(m4.getModifiers()), "windowDeiconified public abstract");
        Method m5 = c.getMethod("windowActivated", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m5.getModifiers()) && Modifier.isAbstract(m5.getModifiers()), "windowActivated public abstract");
        Method m6 = c.getMethod("windowDeactivated", java.awt.event.WindowEvent.class);
        check(Modifier.isPublic(m6.getModifiers()) && Modifier.isAbstract(m6.getModifiers()), "windowDeactivated public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        WindowListener l = new WindowListener() {
            public void windowOpened(java.awt.event.WindowEvent e) { calls.add("windowOpened"); }
            public void windowClosing(java.awt.event.WindowEvent e) { calls.add("windowClosing"); }
            public void windowClosed(java.awt.event.WindowEvent e) { calls.add("windowClosed"); }
            public void windowIconified(java.awt.event.WindowEvent e) { calls.add("windowIconified"); }
            public void windowDeiconified(java.awt.event.WindowEvent e) { calls.add("windowDeiconified"); }
            public void windowActivated(java.awt.event.WindowEvent e) { calls.add("windowActivated"); }
            public void windowDeactivated(java.awt.event.WindowEvent e) { calls.add("windowDeactivated"); }
        };
        l.windowOpened(null);
        check(calls.size() == 1 && calls.get(0).equals("windowOpened"), "dispatch windowOpened");
        l.windowClosing(null);
        check(calls.size() == 2 && calls.get(1).equals("windowClosing"), "dispatch windowClosing");
        l.windowClosed(null);
        check(calls.size() == 3 && calls.get(2).equals("windowClosed"), "dispatch windowClosed");
        l.windowIconified(null);
        check(calls.size() == 4 && calls.get(3).equals("windowIconified"), "dispatch windowIconified");
        l.windowDeiconified(null);
        check(calls.size() == 5 && calls.get(4).equals("windowDeiconified"), "dispatch windowDeiconified");
        l.windowActivated(null);
        check(calls.size() == 6 && calls.get(5).equals("windowActivated"), "dispatch windowActivated");
        l.windowDeactivated(null);
        check(calls.size() == 7 && calls.get(6).equals("windowDeactivated"), "dispatch windowDeactivated");
        check(l instanceof java.util.EventListener, "WindowListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
