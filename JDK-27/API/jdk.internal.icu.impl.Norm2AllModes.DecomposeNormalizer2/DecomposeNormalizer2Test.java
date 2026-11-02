/*
 * @test
 * @summary jdk.internal.icu.impl.Norm2AllModes.DecomposeNormalizer2 conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main DecomposeNormalizer2Test
 */
public class DecomposeNormalizer2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.Norm2AllModes$DecomposeNormalizer2", false, DecomposeNormalizer2Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.Norm2AllModes$DecomposeNormalizer2"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.icu.impl.Norm2AllModes$Normalizer2WithImpl"), "superclass jdk.internal.icu.impl.Norm2AllModes$Normalizer2WithImpl");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 4, "declared methods >= 4");
        check(hasM(ms, "hasBoundaryBefore"), "method hasBoundaryBefore");
        check(hasM(ms, "normalize"), "method normalize");
        check(hasM(ms, "normalizeAndAppend"), "method normalizeAndAppend");
        check(hasM(ms, "spanQuickCheckYes"), "method spanQuickCheckYes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
