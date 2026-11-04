/*
 * @test
 * @summary sun.nio.cs.UnicodeEncoder conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main UnicodeEncoderTest
 */
public class UnicodeEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.UnicodeEncoder", false, UnicodeEncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canEncode"), "method canEncode");
        check(hasMethod(c, "encodeLoop"), "method encodeLoop");
        check(hasMethod(c, "implReset"), "method implReset");
        check(hasField(c, "BIG"), "field BIG");
        check(hasField(c, "BYTE_ORDER_MARK"), "field BYTE_ORDER_MARK");
        check(hasField(c, "LITTLE"), "field LITTLE");
        check(hasField(c, "REVERSED_MARK"), "field REVERSED_MARK");
        check(hasField(c, "byteOrder"), "field byteOrder");
        check(hasField(c, "needsMark"), "field needsMark");
        check(hasField(c, "sgp"), "field sgp");
        check(hasField(c, "usesMark"), "field usesMark");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
