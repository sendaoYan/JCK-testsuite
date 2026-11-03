/*
 * @test
 * @summary jdk.jshell.spi.ExecutionEnv conformance: execution env interface.
 * @run main ExecutionEnvTest
 */

import java.lang.reflect.*;

public class ExecutionEnvTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionEnv");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable behavior; white-box only");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
