/*
 * @test
 * @summary jdk.internal.icu.impl.NormalizerImpl.Hangul conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main HangulTest
 */
public class HangulTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.NormalizerImpl$Hangul", false, HangulTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.NormalizerImpl$Hangul"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 3, "declared methods >= 3");
        check(hasM(ms, "decompose"), "method decompose");
        check(hasM(ms, "isHangul"), "method isHangul");
        check(hasM(ms, "isHangulLV"), "method isHangulLV");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        check(hasF(fs, "HANGUL_BASE"), "field HANGUL_BASE");
        check(hasF(fs, "HANGUL_COUNT"), "field HANGUL_COUNT");
        check(hasF(fs, "HANGUL_END"), "field HANGUL_END");
        check(hasF(fs, "HANGUL_LIMIT"), "field HANGUL_LIMIT");
        check(hasF(fs, "JAMO_L_BASE"), "field JAMO_L_BASE");
        check(hasF(fs, "JAMO_L_COUNT"), "field JAMO_L_COUNT");
        check(hasF(fs, "JAMO_T_BASE"), "field JAMO_T_BASE");
        check(hasF(fs, "JAMO_T_COUNT"), "field JAMO_T_COUNT");
        check(hasF(fs, "JAMO_V_BASE"), "field JAMO_V_BASE");
        check(hasF(fs, "JAMO_V_COUNT"), "field JAMO_V_COUNT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
