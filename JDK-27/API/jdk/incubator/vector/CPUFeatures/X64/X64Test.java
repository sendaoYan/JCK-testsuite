/*
 * @test
 * @summary jdk.incubator.vector.CPUFeatures.X64 conformance: X64 feature flags reflection.
 * @modules jdk.incubator.vector
 * @run main X64Test
 */

import java.lang.reflect.*;

public class X64Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.CPUFeatures$X64");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        Field[] fs = c.getFields();
        boolean hasSupports = false;
        for (Field f : fs) if (f.getName().startsWith("SUPPORTS_")) hasSupports = true;
        check(hasSupports, "has SUPPORTS_* fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
