/*
 * @test
 * @summary jdk.internal.util.DecimalDigits conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main DecimalDigitsTest
 */
public class DecimalDigitsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.DecimalDigits", false, DecimalDigitsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.DecimalDigits"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "appendPair"), "method appendPair");
        check(hasM(ms, "appendQuad"), "method appendQuad");
        check(hasM(ms, "getChars"), "method getChars");
        check(hasM(ms, "putPair"), "method putPair");
        check(hasM(ms, "stringSize"), "method stringSize");
        check(hasM(ms, "uncheckedGetCharsLatin1"), "method uncheckedGetCharsLatin1");
        check(hasM(ms, "uncheckedGetCharsUTF16"), "method uncheckedGetCharsUTF16");
        check(hasM(ms, "uncheckedPutPairLatin1"), "method uncheckedPutPairLatin1");
        check(hasM(ms, "uncheckedPutPairUTF16"), "method uncheckedPutPairUTF16");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
