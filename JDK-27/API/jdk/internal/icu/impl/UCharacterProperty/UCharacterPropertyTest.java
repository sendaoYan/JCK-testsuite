/*
 * @test
 * @summary jdk.internal.icu.impl.UCharacterProperty conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main UCharacterPropertyTest
 */
public class UCharacterPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.UCharacterProperty", false, UCharacterPropertyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.UCharacterProperty"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 13, "declared methods >= 13");
        check(hasM(ms, "digit"), "method digit");
        check(hasM(ms, "getAdditional"), "method getAdditional");
        check(hasM(ms, "getAge"), "method getAge");
        check(hasM(ms, "getEuropeanDigit"), "method getEuropeanDigit");
        check(hasM(ms, "getIntPropertyValue"), "method getIntPropertyValue");
        check(hasM(ms, "getMask"), "method getMask");
        check(hasM(ms, "getProperty"), "method getProperty");
        check(hasM(ms, "getRawSupplementary"), "method getRawSupplementary");
        check(hasM(ms, "getType"), "method getType");
        check(hasM(ms, "mergeScriptCodeOrIndex"), "method mergeScriptCodeOrIndex");
        check(hasM(ms, "upropsvec_addPropertyStarts"), "method upropsvec_addPropertyStarts");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 82, "declared fields >= 82");
        check(hasF(fs, "BIDI_PAIRED_BRACKET_TYPE"), "field BIDI_PAIRED_BRACKET_TYPE");
        check(hasF(fs, "INSTANCE"), "field INSTANCE");
        check(hasF(fs, "MAX_SCRIPT"), "field MAX_SCRIPT");
        check(hasF(fs, "SCRIPT_HIGH_MASK"), "field SCRIPT_HIGH_MASK");
        check(hasF(fs, "SCRIPT_HIGH_SHIFT"), "field SCRIPT_HIGH_SHIFT");
        check(hasF(fs, "SCRIPT_LOW_MASK"), "field SCRIPT_LOW_MASK");
        check(hasF(fs, "SCRIPT_X_MASK"), "field SCRIPT_X_MASK");
        check(hasF(fs, "SRC_BIDI"), "field SRC_BIDI");
        check(hasF(fs, "SRC_CHAR"), "field SRC_CHAR");
        check(hasF(fs, "SRC_NFC"), "field SRC_NFC");
        check(hasF(fs, "SRC_NFKC"), "field SRC_NFKC");
        check(hasF(fs, "SRC_PROPSVEC"), "field SRC_PROPSVEC");
        check(hasF(fs, "TYPE_MASK"), "field TYPE_MASK");
        check(hasF(fs, "m_scriptExtensions_"), "field m_scriptExtensions_");
        check(hasF(fs, "m_trie_"), "field m_trie_");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
