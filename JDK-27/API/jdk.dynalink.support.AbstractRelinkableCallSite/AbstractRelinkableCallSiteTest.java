/*
 * @test
 * @summary jdk.dynalink.support.AbstractRelinkableCallSite conformance: abstract call site.
 * @run main AbstractRelinkableCallSiteTest
 */

import java.lang.reflect.*;

public class AbstractRelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.support.AbstractRelinkableCallSite");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.dynalink.RelinkableCallSite.class.isAssignableFrom(c), "implements RelinkableCallSite");
        check(java.lang.invoke.MutableCallSite.class.isAssignableFrom(c), "extends MutableCallSite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
