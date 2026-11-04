/*
 * @test
 * @summary sun.net.www.ParseUtil conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main ParseUtilTest
 */
public class ParseUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.ParseUtil", false, ParseUtilTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decode"), "method decode");
        check(hasMethod(c, "encodePath"), "method encodePath");
        check(hasMethod(c, "fileToEncodedURL"), "method fileToEncodedURL");
        check(hasMethod(c, "isLocalFileURL"), "method isLocalFileURL");
        check(hasMethod(c, "toURI"), "method toURI");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "HEX_UPPERCASE"), "field HEX_UPPERCASE");
        check(hasField(c, "H_ALPHA"), "field H_ALPHA");
        check(hasField(c, "H_ALPHANUM"), "field H_ALPHANUM");
        check(hasField(c, "H_DASH"), "field H_DASH");
        check(hasField(c, "H_DIGIT"), "field H_DIGIT");
        check(hasField(c, "H_ENCODED"), "field H_ENCODED");
        check(hasField(c, "H_ESCAPED"), "field H_ESCAPED");
        check(hasField(c, "H_HEX"), "field H_HEX");
        check(hasField(c, "H_LOWALPHA"), "field H_LOWALPHA");
        check(hasField(c, "H_MARK"), "field H_MARK");
        check(hasField(c, "H_PATH"), "field H_PATH");
        check(hasField(c, "H_PCHAR"), "field H_PCHAR");
        check(hasField(c, "H_REG_NAME"), "field H_REG_NAME");
        check(hasField(c, "H_RESERVED"), "field H_RESERVED");
        check(hasField(c, "H_SERVER"), "field H_SERVER");
        check(hasField(c, "H_UNRESERVED"), "field H_UNRESERVED");
        check(hasField(c, "H_UPALPHA"), "field H_UPALPHA");
        check(hasField(c, "H_URIC"), "field H_URIC");
        check(hasField(c, "H_USERINFO"), "field H_USERINFO");
        check(hasField(c, "L_ALPHA"), "field L_ALPHA");
        check(hasField(c, "L_ALPHANUM"), "field L_ALPHANUM");
        check(hasField(c, "L_DASH"), "field L_DASH");
        check(hasField(c, "L_DIGIT"), "field L_DIGIT");
        check(hasField(c, "L_ENCODED"), "field L_ENCODED");
        check(hasField(c, "L_ESCAPED"), "field L_ESCAPED");
        check(hasField(c, "L_HEX"), "field L_HEX");
        check(hasField(c, "L_LOWALPHA"), "field L_LOWALPHA");
        check(hasField(c, "L_MARK"), "field L_MARK");
        check(hasField(c, "L_PATH"), "field L_PATH");
        check(hasField(c, "L_PCHAR"), "field L_PCHAR");
        check(hasField(c, "L_REG_NAME"), "field L_REG_NAME");
        check(hasField(c, "L_RESERVED"), "field L_RESERVED");
        check(hasField(c, "L_SERVER"), "field L_SERVER");
        check(hasField(c, "L_UNRESERVED"), "field L_UNRESERVED");
        check(hasField(c, "L_UPALPHA"), "field L_UPALPHA");
        check(hasField(c, "L_URIC"), "field L_URIC");
        check(hasField(c, "L_USERINFO"), "field L_USERINFO");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
