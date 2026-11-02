/*
 * @test
 * @summary jdk.dynalink.linker.MethodTypeConversionStrategy conformance: interface reflection.
 * @run main MethodTypeConversionStrategyTest
 */

import java.lang.reflect.*;

public class MethodTypeConversionStrategyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.MethodTypeConversionStrategy");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
