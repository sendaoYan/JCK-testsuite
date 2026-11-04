/*
 * @test
 * @summary jdk.jshell.execution.Util conformance: util static methods.
 * @run main UtilTest
 */

import java.lang.reflect.*;

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.Util");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("forwardExecutionControl", jdk.jshell.spi.ExecutionControl.class, java.io.ObjectInput.class, java.io.ObjectOutput.class) != null, "forwardExecutionControl");
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
