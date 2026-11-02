/*
 * @test
 * @summary jdk.jshell.tool.JavaShellToolBuilder conformance: tool builder interface.
 * @run main JavaShellToolBuilderTest
 */

import java.lang.reflect.*;

public class JavaShellToolBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.tool.JavaShellToolBuilder");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
