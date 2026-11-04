/*
 * @test
 * @summary java.awt.event.TextListener conformance: white-box structure + black-box behavior.
 * @run main TextListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.TextListener;

public class TextListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.TextListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "TextListener is public");
        check(c.isInterface(), "TextListener is interface");
        check(c.getSuperclass() == null, "TextListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "TextListener extends EventListener");
        check(c.getDeclaredMethods().length == 1, "TextListener has 1 methods");
        Method m0 = c.getMethod("textValueChanged", java.awt.event.TextEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "textValueChanged public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        TextListener l = new TextListener() {
            public void textValueChanged(java.awt.event.TextEvent e) { calls.add("textValueChanged"); }
        };
        l.textValueChanged(null);
        check(calls.size() == 1 && calls.get(0).equals("textValueChanged"), "dispatch textValueChanged");
        final java.util.List<String> lam = new java.util.ArrayList<String>();
        TextListener lambda = (e) -> lam.add("x");
        lambda.textValueChanged(null);
        check(lam.size() == 1 && lam.get(0).equals("x"), "SAM lambda invocation");
        check(l instanceof java.util.EventListener, "TextListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
