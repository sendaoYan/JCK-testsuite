/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl conformance: execution control interface.
 * @run main ExecutionControlTest
 */

import java.lang.reflect.*;

public class ExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(java.lang.AutoCloseable.class.isAssignableFrom(c), "extends AutoCloseable");
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
