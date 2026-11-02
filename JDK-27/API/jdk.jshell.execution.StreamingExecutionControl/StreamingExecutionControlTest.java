/*
 * @test
 * @summary jdk.jshell.execution.StreamingExecutionControl conformance: streaming execution control.
 * @run main StreamingExecutionControlTest
 */

import java.lang.reflect.*;

public class StreamingExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.StreamingExecutionControl");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.jshell.spi.ExecutionControl.class.isAssignableFrom(c), "implements ExecutionControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
