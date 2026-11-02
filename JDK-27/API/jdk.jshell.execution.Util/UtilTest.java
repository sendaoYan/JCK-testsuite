/*
 * @test
 * @summary jdk.jshell.execution.Util conformance: util static methods.
 * @run main UtilTest
 */

import java.lang.reflect.*;

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.Util");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("forwardExecutionControl", jdk.jshell.spi.ExecutionControl.class, java.io.ObjectInput.class, java.io.ObjectOutput.class) != null, "forwardExecutionControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
