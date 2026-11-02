/*
 * @test
 * @summary jdk.jshell.execution.LoaderDelegate conformance: loader delegate interface.
 * @run main LoaderDelegateTest
 */

import java.lang.reflect.*;

public class LoaderDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.LoaderDelegate");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
