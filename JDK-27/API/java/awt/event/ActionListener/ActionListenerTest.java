/*
 * @test
 * @summary java.awt.event.ActionListener conformance: white-box structure + black-box behavior.
 * @run main ActionListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ActionListener;

public class ActionListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ActionListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ActionListener is public");
        check(c.isInterface(), "ActionListener is interface");
        check(c.getSuperclass() == null, "ActionListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "ActionListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "ActionListener has 1 methods");
        Method m0 = c.getMethod("actionPerformed", java.awt.event.ActionEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "actionPerformed public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ActionListener l = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { calls.add("actionPerformed"); }
        };
        l.actionPerformed(null);
        check(calls.size() == 1 && calls.get(0).equals("actionPerformed"), "dispatch actionPerformed");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        ActionListener lambda = (e) -> lam.add("x");
        lambda.actionPerformed(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "ActionListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
