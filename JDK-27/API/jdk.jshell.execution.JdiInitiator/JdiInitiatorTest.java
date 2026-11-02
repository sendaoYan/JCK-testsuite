/*
 * @test
 * @summary jdk.jshell.execution.JdiInitiator conformance: jdi initiator shape.
 * @run main JdiInitiatorTest
 */

import java.lang.reflect.*;

public class JdiInitiatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiInitiator");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("vm") != null, "vm method");
        check(c.getMethod("process") != null, "process method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
