/*
 * @test
 * @summary jdk.jshell.execution.JdiInitiator.ProcessStarted conformance: protected nested interface.
 * @run main ProcessStartedTest
 */

import java.lang.reflect.*;

public class ProcessStartedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiInitiator$ProcessStarted");
        check(c.isInterface(), "is interface");
        check(Modifier.isProtected(c.getModifiers()), "is protected");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
