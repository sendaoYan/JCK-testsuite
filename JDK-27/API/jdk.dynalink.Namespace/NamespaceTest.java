/*
 * @test
 * @summary jdk.dynalink.Namespace conformance: interface reflection.
 * @run main NamespaceTest
 */

import java.lang.reflect.*;

public class NamespaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.Namespace");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
