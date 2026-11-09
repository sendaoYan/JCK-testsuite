/*
 * @test
 * @summary jdk.incubator.vector.CPUFeatures.X64 conformance: X64 feature flags reflection.
 * @modules jdk.incubator.vector
 * @requires (os.arch == "amd64")
 * @run main X64Test
 */

import java.lang.reflect.*;

public class X64Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.CPUFeatures$X64");
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (Field f : c.getFields()) names.add(f.getName());
        check(names.equals(new java.util.TreeSet<>(java.util.Arrays.asList(
                "SUPPORTS_AVX", "SUPPORTS_AVX2", "SUPPORTS_AVX512F", "SUPPORTS_AVX512DQ"))),
                "exactly four x64 feature flags: " + names);
        for (Field f : c.getFields()) {
            check(f.getType() == boolean.class, f.getName() + " boolean");
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()), f.getName() + " public static");
            check(!Modifier.isFinal(f.getModifiers()), f.getName() + " intentionally non-final");
            check(f.getBoolean(null) == f.getBoolean(null), f.getName() + " reads consistently");
        }
        Class<?> outer = Class.forName("jdk.incubator.vector.CPUFeatures");
        check(!Modifier.isPublic(outer.getModifiers()), "CPUFeatures package-private");
        check(Modifier.isStatic(c.getModifiers()) && Modifier.isPublic(c.getModifiers()), "X64 public static nested");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

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
