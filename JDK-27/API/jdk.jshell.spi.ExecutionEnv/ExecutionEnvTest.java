/*
 * @test
 * @summary jdk.jshell.spi.ExecutionEnv conformance: execution env interface.
 * @run main ExecutionEnvTest
 */

import java.lang.reflect.*;

public class ExecutionEnvTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionEnv");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
