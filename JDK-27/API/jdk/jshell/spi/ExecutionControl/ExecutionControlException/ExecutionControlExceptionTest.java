/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ExecutionControlException conformance: execution control exception abstract.
 * @run main ExecutionControlExceptionTest
 */

import java.lang.reflect.*;

public class ExecutionControlExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$ExecutionControlException");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
