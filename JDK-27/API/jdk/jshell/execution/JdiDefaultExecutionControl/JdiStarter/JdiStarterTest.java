/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter conformance: jdi starter interface.
 * @run main JdiStarterTest
 */

import java.lang.reflect.*;

public class JdiStarterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
