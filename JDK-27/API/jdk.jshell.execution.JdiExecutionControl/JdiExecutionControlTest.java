/*
 * @test
 * @summary jdk.jshell.execution.JdiExecutionControl conformance: jdi execution control abstract.
 * @run main JdiExecutionControlTest
 */

import java.lang.reflect.*;

public class JdiExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControl");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
