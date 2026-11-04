/*
 * @test
 * @summary jdk.internal.icu.lang.UCharacter conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.lang:+open
 * @run main UCharacterTest
 */
public class UCharacterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.lang.UCharacter", false, UCharacterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.lang.UCharacter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 11, "declared methods >= 11");
        check(hasM(ms, "digit"), "method digit");
        check(hasM(ms, "getAge"), "method getAge");
        check(hasM(ms, "getBidiPairedBracket"), "method getBidiPairedBracket");
        check(hasM(ms, "getCodePoint"), "method getCodePoint");
        check(hasM(ms, "getCombiningClass"), "method getCombiningClass");
        check(hasM(ms, "getDirection"), "method getDirection");
        check(hasM(ms, "getIntPropertyValue"), "method getIntPropertyValue");
        check(hasM(ms, "getMirror"), "method getMirror");
        check(hasM(ms, "getType"), "method getType");
        check(hasM(ms, "getUnicodeVersion"), "method getUnicodeVersion");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 8, "declared fields >= 8");
        check(hasF(fs, "CHAR_CATEGORY_COUNT"), "field CHAR_CATEGORY_COUNT");
        check(hasF(fs, "COMBINING_SPACING_MARK"), "field COMBINING_SPACING_MARK");
        check(hasF(fs, "ENCLOSING_MARK"), "field ENCLOSING_MARK");
        check(hasF(fs, "MAX_VALUE"), "field MAX_VALUE");
        check(hasF(fs, "MIN_VALUE"), "field MIN_VALUE");
        check(hasF(fs, "NON_SPACING_MARK"), "field NON_SPACING_MARK");
        check(hasF(fs, "RIGHT_TO_LEFT"), "field RIGHT_TO_LEFT");
        check(hasF(fs, "RIGHT_TO_LEFT_ARABIC"), "field RIGHT_TO_LEFT_ARABIC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
