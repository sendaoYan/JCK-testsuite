/*
 * @test
 * @summary jdk.internal.icu.text.UTF16 conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main UTF16Test
 */
public class UTF16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.UTF16", false, UTF16Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.UTF16"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 15, "declared methods >= 15");
        check(hasM(ms, "append"), "method append");
        check(hasM(ms, "charAt"), "method charAt");
        check(hasM(ms, "getCharCount"), "method getCharCount");
        check(hasM(ms, "getLeadSurrogate"), "method getLeadSurrogate");
        check(hasM(ms, "getTrailSurrogate"), "method getTrailSurrogate");
        check(hasM(ms, "isLeadSurrogate"), "method isLeadSurrogate");
        check(hasM(ms, "isSurrogate"), "method isSurrogate");
        check(hasM(ms, "isTrailSurrogate"), "method isTrailSurrogate");
        check(hasM(ms, "moveCodePointOffset"), "method moveCodePointOffset");
        check(hasM(ms, "valueOf"), "method valueOf");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 17, "declared fields >= 17");
        check(hasF(fs, "CODEPOINT_MAX_VALUE"), "field CODEPOINT_MAX_VALUE");
        check(hasF(fs, "CODEPOINT_MIN_VALUE"), "field CODEPOINT_MIN_VALUE");
        check(hasF(fs, "LEAD_SURROGATE_MAX_VALUE"), "field LEAD_SURROGATE_MAX_VALUE");
        check(hasF(fs, "LEAD_SURROGATE_MIN_VALUE"), "field LEAD_SURROGATE_MIN_VALUE");
        check(hasF(fs, "SUPPLEMENTARY_MIN_VALUE"), "field SUPPLEMENTARY_MIN_VALUE");
        check(hasF(fs, "SURROGATE_MIN_VALUE"), "field SURROGATE_MIN_VALUE");
        check(hasF(fs, "TRAIL_SURROGATE_MAX_VALUE"), "field TRAIL_SURROGATE_MAX_VALUE");
        check(hasF(fs, "TRAIL_SURROGATE_MIN_VALUE"), "field TRAIL_SURROGATE_MIN_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
