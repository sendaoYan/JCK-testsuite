/*
 * @test
 * @summary jdk.internal.icu.impl.NormalizerImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main NormalizerImplTest
 */
public class NormalizerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.NormalizerImpl", false, NormalizerImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.NormalizerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 70, "declared methods >= 70");
        check(hasM(ms, "canonicalDecomposeWithSingleQuotation"), "method canonicalDecomposeWithSingleQuotation");
        check(hasM(ms, "compose"), "method compose");
        check(hasM(ms, "composeAndAppend"), "method composeAndAppend");
        check(hasM(ms, "composeQuickCheck"), "method composeQuickCheck");
        check(hasM(ms, "decompose"), "method decompose");
        check(hasM(ms, "decomposeAndAppend"), "method decomposeAndAppend");
        check(hasM(ms, "getCC"), "method getCC");
        check(hasM(ms, "getCCFromNormalYesOrMaybe"), "method getCCFromNormalYesOrMaybe");
        check(hasM(ms, "getCCFromYesOrMaybeYes"), "method getCCFromYesOrMaybeYes");
        check(hasM(ms, "getCCFromYesOrMaybeYesCP"), "method getCCFromYesOrMaybeYesCP");
        check(hasM(ms, "getDecompose"), "method getDecompose");
        check(hasM(ms, "getDecomposition"), "method getDecomposition");
        check(hasM(ms, "getFCD16"), "method getFCD16");
        check(hasM(ms, "getFCD16FromNormData"), "method getFCD16FromNormData");
        check(hasM(ms, "getNorm16"), "method getNorm16");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 63, "declared fields >= 63");
        check(hasF(fs, "COMP_1_LAST_TUPLE"), "field COMP_1_LAST_TUPLE");
        check(hasF(fs, "COMP_1_TRAIL_LIMIT"), "field COMP_1_TRAIL_LIMIT");
        check(hasF(fs, "COMP_1_TRAIL_MASK"), "field COMP_1_TRAIL_MASK");
        check(hasF(fs, "COMP_1_TRAIL_SHIFT"), "field COMP_1_TRAIL_SHIFT");
        check(hasF(fs, "COMP_1_TRIPLE"), "field COMP_1_TRIPLE");
        check(hasF(fs, "COMP_2_TRAIL_MASK"), "field COMP_2_TRAIL_MASK");
        check(hasF(fs, "COMP_2_TRAIL_SHIFT"), "field COMP_2_TRAIL_SHIFT");
        check(hasF(fs, "DELTA_SHIFT"), "field DELTA_SHIFT");
        check(hasF(fs, "DELTA_TCCC_0"), "field DELTA_TCCC_0");
        check(hasF(fs, "DELTA_TCCC_1"), "field DELTA_TCCC_1");
        check(hasF(fs, "DELTA_TCCC_GT_1"), "field DELTA_TCCC_GT_1");
        check(hasF(fs, "DELTA_TCCC_MASK"), "field DELTA_TCCC_MASK");
        check(hasF(fs, "HAS_COMP_BOUNDARY_AFTER"), "field HAS_COMP_BOUNDARY_AFTER");
        check(hasF(fs, "INERT"), "field INERT");
        check(hasF(fs, "IX_EXTRA_DATA_OFFSET"), "field IX_EXTRA_DATA_OFFSET");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
