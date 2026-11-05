/*
 * @test
 * @summary jdk.jshell.execution.JdiInitiator.ProcessStarted conformance: protected nested interface.
 * @run main ProcessStartedTest
 */

import java.lang.reflect.*;

public class ProcessStartedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: single-abstract-method hook receiving the debuggee process ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiInitiator$ProcessStarted");
        int abs = 0;
        String sig = "";
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abs++; sig = m.toGenericString(); }
        check(abs == 1, "exactly one abstract method");
        check(sig.contains("processStarted"), "method is processStarted: " + sig);
        check(sig.contains("Process"), "parameter type is Process");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiInitiator$ProcessStarted");
        check(c.isInterface(), "is interface");
        check(Modifier.isProtected(c.getModifiers()), "is protected");
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
