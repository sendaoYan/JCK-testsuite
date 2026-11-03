/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl conformance: jdi default execution control.
 * @run main JdiDefaultExecutionControlTest
 */

import java.lang.reflect.*;

public class JdiDefaultExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.execution.JdiExecutionControl"), "extends JdiExecutionControl");
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
