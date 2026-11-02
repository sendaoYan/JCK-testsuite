/*
 * @test
 * @summary jdk.dynalink.RelinkableCallSite conformance: interface reflection.
 * @run main RelinkableCallSiteTest
 */

import java.lang.reflect.*;

public class RelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.RelinkableCallSite");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
