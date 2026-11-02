/*
 * @test
 * @summary jdk.internal.icu.impl.UnicodeSetStringSpan conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main UnicodeSetStringSpanTest
 */
public class UnicodeSetStringSpanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.UnicodeSetStringSpan", false, UnicodeSetStringSpanTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.UnicodeSetStringSpan"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 15, "declared methods >= 15");
        check(hasM(ms, "contains"), "method contains");
        check(hasM(ms, "needsStringSpanUTF16"), "method needsStringSpanUTF16");
        check(hasM(ms, "span"), "method span");
        check(hasM(ms, "spanAndCount"), "method spanAndCount");
        check(hasM(ms, "spanBack"), "method spanBack");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 20, "declared fields >= 20");
        check(hasF(fs, "ALL"), "field ALL");
        check(hasF(fs, "BACK"), "field BACK");
        check(hasF(fs, "BACK_UTF16_CONTAINED"), "field BACK_UTF16_CONTAINED");
        check(hasF(fs, "BACK_UTF16_NOT_CONTAINED"), "field BACK_UTF16_NOT_CONTAINED");
        check(hasF(fs, "CONTAINED"), "field CONTAINED");
        check(hasF(fs, "FWD"), "field FWD");
        check(hasF(fs, "FWD_UTF16_CONTAINED"), "field FWD_UTF16_CONTAINED");
        check(hasF(fs, "FWD_UTF16_NOT_CONTAINED"), "field FWD_UTF16_NOT_CONTAINED");
        check(hasF(fs, "NOT_CONTAINED"), "field NOT_CONTAINED");
        check(hasF(fs, "WITH_COUNT"), "field WITH_COUNT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
