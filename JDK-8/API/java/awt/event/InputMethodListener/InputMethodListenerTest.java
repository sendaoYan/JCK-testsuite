/*
 * @test
 * @summary java.awt.event.InputMethodListener conformance: white-box structure + black-box behavior.
 * @run main InputMethodListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.InputMethodListener;

public class InputMethodListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.InputMethodListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "InputMethodListener is public");
        check(c.isInterface(), "InputMethodListener is interface");
        check(c.getSuperclass() == null, "InputMethodListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "InputMethodListener extends EventListener");
        check(c.getDeclaredMethods().length == 2, "InputMethodListener has 2 methods");
        Method m0 = c.getMethod("inputMethodTextChanged", java.awt.event.InputMethodEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "inputMethodTextChanged public abstract");
        Method m1 = c.getMethod("caretPositionChanged", java.awt.event.InputMethodEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "caretPositionChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        InputMethodListener l = new InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent e) { calls.add("inputMethodTextChanged"); }
            public void caretPositionChanged(java.awt.event.InputMethodEvent e) { calls.add("caretPositionChanged"); }
        };
        l.inputMethodTextChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("inputMethodTextChanged"), "dispatch inputMethodTextChanged");
        l.caretPositionChanged(null);
        check(calls.size() == 2 && calls.get(1).equals("caretPositionChanged"), "dispatch caretPositionChanged");
        check(l instanceof java.util.EventListener, "InputMethodListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
