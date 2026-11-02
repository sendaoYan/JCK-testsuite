/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.RunException conformance: run exception abstract.
 * @run main RunExceptionTest
 */

import java.lang.reflect.*;

public class RunExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$RunException");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.spi.ExecutionControl$ExecutionControlException"), "extends ExecutionControlException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
