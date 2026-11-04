/*
 * @test
 * @summary jdk.internal.icu.lang.UCharacter.HangulSyllableType conformance: internal interface reflection.
 * @modules java.base/jdk.internal.icu.lang:+open
 * @run main HangulSyllableTypeTest
 */
public class HangulSyllableTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.lang.UCharacter$HangulSyllableType", false, HangulSyllableTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.lang.UCharacter$HangulSyllableType"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 7, "declared fields >= 7");
        check(hasF(fs, "COUNT"), "field COUNT");
        check(hasF(fs, "LEADING_JAMO"), "field LEADING_JAMO");
        check(hasF(fs, "LVT_SYLLABLE"), "field LVT_SYLLABLE");
        check(hasF(fs, "LV_SYLLABLE"), "field LV_SYLLABLE");
        check(hasF(fs, "NOT_APPLICABLE"), "field NOT_APPLICABLE");
        check(hasF(fs, "TRAILING_JAMO"), "field TRAILING_JAMO");
        check(hasF(fs, "VOWEL_JAMO"), "field VOWEL_JAMO");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
