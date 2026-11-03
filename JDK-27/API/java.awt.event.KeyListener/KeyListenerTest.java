/*
 * @test
 * @summary java.awt.event.KeyListener conformance: white-box structure + black-box behavior.
 * @run main KeyListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.event.KeyListener;

public class KeyListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.KeyListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "KeyListener is public");
        check(c.isInterface(), "KeyListener is interface");
        check(c.getSuperclass() == null, "KeyListener has no superclass");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(java.util.EventListener.class), "KeyListener extends EventListener");
        check(c.getDeclaredMethods().length == 3, "KeyListener has 3 methods");
        Method m0 = c.getMethod("keyTyped", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m0.getModifiers()) && Modifier.isAbstract(m0.getModifiers()), "keyTyped public abstract");
        Method m1 = c.getMethod("keyPressed", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m1.getModifiers()) && Modifier.isAbstract(m1.getModifiers()), "keyPressed public abstract");
        Method m2 = c.getMethod("keyReleased", java.awt.event.KeyEvent.class);
        check(Modifier.isPublic(m2.getModifiers()) && Modifier.isAbstract(m2.getModifiers()), "keyReleased public abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        KeyListener l = new KeyListener() {
            public void keyTyped(java.awt.event.KeyEvent e) { calls.add("keyTyped"); }
            public void keyPressed(java.awt.event.KeyEvent e) { calls.add("keyPressed"); }
            public void keyReleased(java.awt.event.KeyEvent e) { calls.add("keyReleased"); }
        };
        l.keyTyped(null);
        check(calls.size() == 1 && calls.get(0).equals("keyTyped"), "dispatch keyTyped");
        l.keyPressed(null);
        check(calls.size() == 2 && calls.get(1).equals("keyPressed"), "dispatch keyPressed");
        l.keyReleased(null);
        check(calls.size() == 3 && calls.get(2).equals("keyReleased"), "dispatch keyReleased");
        check(l instanceof java.util.EventListener, "KeyListener instanceof EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
