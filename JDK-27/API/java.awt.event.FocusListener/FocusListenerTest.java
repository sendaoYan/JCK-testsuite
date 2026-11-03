/*
 * @test
 * @summary java.awt.event.FocusListener conformance: white-box structure + black-box behavior.
 * @run main FocusListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.FocusListener;

public class FocusListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.FocusListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "FocusListener is public");
        check(c.isInterface(), "FocusListener is interface");
        check(c.getSuperclass() == null, "FocusListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "FocusListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "FocusListener has 2 methods");
        Method m0 = c.getMethod("focusGained", java.awt.event.FocusEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "focusGained public abstract");
        Method m1 = c.getMethod("focusLost", java.awt.event.FocusEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "focusLost public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        FocusListener l = new FocusListener() {
            public void focusGained(java.awt.event.FocusEvent e) { calls.add("focusGained"); }
            public void focusLost(java.awt.event.FocusEvent e) { calls.add("focusLost"); }
        };
        l.focusGained(null);
        check(calls.size() == 1 && calls.get(0).equals("focusGained"), "dispatch focusGained");
        l.focusLost(null);
        check(calls.size() == 2 && calls.get(1).equals("focusLost"), "dispatch focusLost");
        check(l instanceof java.util.EventListener, "FocusListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
