/*
 * @test
 * @summary java.awt.event.AdjustmentListener conformance: white-box structure + black-box behavior.
 * @run main AdjustmentListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.AdjustmentListener;

public class AdjustmentListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.AdjustmentListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AdjustmentListener is public");
        check(c.isInterface(), "AdjustmentListener is interface");
        check(c.getSuperclass() == null, "AdjustmentListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "AdjustmentListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "AdjustmentListener has 1 methods");
        Method m0 = c.getMethod("adjustmentValueChanged", java.awt.event.AdjustmentEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "adjustmentValueChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        AdjustmentListener l = new AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) { calls.add("adjustmentValueChanged"); }
        };
        l.adjustmentValueChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("adjustmentValueChanged"), "dispatch adjustmentValueChanged");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        AdjustmentListener lambda = (e) -> lam.add("x");
        lambda.adjustmentValueChanged(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "AdjustmentListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
