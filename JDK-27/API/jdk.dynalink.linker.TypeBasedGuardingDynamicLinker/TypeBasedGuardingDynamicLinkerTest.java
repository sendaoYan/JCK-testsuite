/*
 * @test
 * @summary jdk.dynalink.linker.TypeBasedGuardingDynamicLinker conformance: interface reflection.
 * @run main TypeBasedGuardingDynamicLinkerTest
 */

import java.lang.reflect.*;

public class TypeBasedGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.TypeBasedGuardingDynamicLinker");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.dynalink.linker.GuardingDynamicLinker.class.isAssignableFrom(c), "extends GuardingDynamicLinker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
