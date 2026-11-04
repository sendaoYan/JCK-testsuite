/*
 * @test
 * @summary java.awt.ActiveEvent conformance: white-box structure + black-box behavior.
 * @run main ActiveEventTest
 */

import java.lang.reflect.*;
import java.awt.ActiveEvent;

public class ActiveEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.ActiveEvent", false, ActiveEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.ActiveEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isInterface(m), "public interface");
        check(c.isInterface(), "is interface");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
        check(c.getMethod("dispatch") != null, "dispatch() present");
        check(c.getMethod("dispatch").getReturnType() == void.class, "dispatch returns void");
        check(Modifier.isPublic(c.getMethod("dispatch").getModifiers()), "dispatch is public");
        check(java.awt.ActiveEvent.class.isInterface(), "ActiveEvent.class is interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final boolean[] dispatched = { false };
        final int[] counter = { 0 };
        ActiveEvent event = new ActiveEvent() {
            public void dispatch() {
                dispatched[0] = true;
                counter[0]++;
            }
        };

        check(!dispatched[0], "not dispatched before call");
        event.dispatch();
        check(dispatched[0], "dispatch() flips flag");
        check(counter[0] == 1, "dispatch() invoked exactly once");
        event.dispatch();
        check(counter[0] == 2, "dispatch() can be invoked repeatedly");
        check(event instanceof ActiveEvent, "anonymous implementation instanceof ActiveEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.ActiveEvent.class.isInterface(), "ActiveEvent is an interface");
        boolean hasDispatch = false, hasDispose = false;
        for (java.lang.reflect.Method m : java.awt.ActiveEvent.class.getMethods()) {
            if (m.getName().equals("dispatch")) hasDispatch = true;
            if (m.getName().equals("isDispatched")) hasDispose = true;
        }
        check(hasDispatch, "declares dispatch()");
        // isDispatched has a default implementation (JDK 9+); dispatch is abstract
        // a minimal implementation must run dispatch exactly when asked
        final int[] ran = new int[1];
        java.awt.ActiveEvent ev = new java.awt.ActiveEvent() {
            public void dispatch() { ran[0]++; }
        };
        check(ran[0] == 0, "not dispatched before dispatch()");
        ev.dispatch();
        check(ran[0] == 1, "dispatch() runs the action");
        ev.dispatch();
        check(ran[0] == 2, "dispatch() repeatable");
    }

}
