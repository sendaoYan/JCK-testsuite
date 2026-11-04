/*
 * @test
 * @summary jdk.internal.icu.text.Normalizer2 conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main Normalizer2Test
 */
public class Normalizer2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.Normalizer2", false, Normalizer2Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.Normalizer2"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "append"), "method append");
        check(hasM(ms, "getCombiningClass"), "method getCombiningClass");
        check(hasM(ms, "getDecomposition"), "method getDecomposition");
        check(hasM(ms, "getNFCInstance"), "method getNFCInstance");
        check(hasM(ms, "getNFDInstance"), "method getNFDInstance");
        check(hasM(ms, "getNFKCInstance"), "method getNFKCInstance");
        check(hasM(ms, "getNFKDInstance"), "method getNFKDInstance");
        check(hasM(ms, "hasBoundaryBefore"), "method hasBoundaryBefore");
        check(hasM(ms, "isNormalized"), "method isNormalized");
        check(hasM(ms, "normalize"), "method normalize");
        check(hasM(ms, "normalizeSecondAndAppend"), "method normalizeSecondAndAppend");
        check(hasM(ms, "spanQuickCheckYes"), "method spanQuickCheckYes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
