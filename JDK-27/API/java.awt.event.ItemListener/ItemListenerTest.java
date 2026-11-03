/*
 * @test
 * @summary java.awt.event.ItemListener conformance: white-box structure + black-box behavior.
 * @run main ItemListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.ItemListener;

public class ItemListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ItemListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ItemListener is public");
        check(c.isInterface(), "ItemListener is interface");
        check(c.getSuperclass() == null, "ItemListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "ItemListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "ItemListener has 1 methods");
        Method m0 = c.getMethod("itemStateChanged", java.awt.event.ItemEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "itemStateChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        ItemListener l = new ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent e) { calls.add("itemStateChanged"); }
        };
        l.itemStateChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("itemStateChanged"), "dispatch itemStateChanged");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        ItemListener lambda = (e) -> lam.add("x");
        lambda.itemStateChanged(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "ItemListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
