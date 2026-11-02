/*
 * @test
 * @summary jdk.internal.icu.text.NormalizerBase conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main NormalizerBaseTest
 */
public class NormalizerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.NormalizerBase", false, NormalizerBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.NormalizerBase"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 26, "declared methods >= 26");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "current"), "method current");
        check(hasM(ms, "endIndex"), "method endIndex");
        check(hasM(ms, "getBeginIndex"), "method getBeginIndex");
        check(hasM(ms, "getEndIndex"), "method getEndIndex");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getMode"), "method getMode");
        check(hasM(ms, "isNormalized"), "method isNormalized");
        check(hasM(ms, "next"), "method next");
        check(hasM(ms, "normalize"), "method normalize");
        check(hasM(ms, "previous"), "method previous");
        check(hasM(ms, "reset"), "method reset");
        check(hasM(ms, "setIndex"), "method setIndex");
        check(hasM(ms, "setIndexOnly"), "method setIndexOnly");
        check(hasM(ms, "setMode"), "method setMode");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 17, "declared fields >= 17");
        check(hasF(fs, "DONE"), "field DONE");
        check(hasF(fs, "NFC"), "field NFC");
        check(hasF(fs, "NFD"), "field NFD");
        check(hasF(fs, "NFKC"), "field NFKC");
        check(hasF(fs, "NFKD"), "field NFKD");
        check(hasF(fs, "NONE"), "field NONE");
        check(hasF(fs, "UNICODE_3_2"), "field UNICODE_3_2");
        check(hasF(fs, "UNICODE_3_2_0_ORIGINAL"), "field UNICODE_3_2_0_ORIGINAL");
        check(hasF(fs, "UNICODE_LATEST"), "field UNICODE_LATEST");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
