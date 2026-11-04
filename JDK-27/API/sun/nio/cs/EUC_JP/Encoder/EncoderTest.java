/*
 * @test
 * @summary sun.nio.cs.EUC_JP.Encoder conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main EncoderTest
 */
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.EUC_JP$Encoder", false, EncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canEncode"), "method canEncode");
        check(hasMethod(c, "encodeDouble"), "method encodeDouble");
        check(hasMethod(c, "encodeLoop"), "method encodeLoop");
        check(hasMethod(c, "encodeSingle"), "method encodeSingle");
        check(hasField(c, "ENC0201"), "field ENC0201");
        check(hasField(c, "ENC0208"), "field ENC0208");
        check(hasField(c, "ENC0212"), "field ENC0212");
        check(hasField(c, "enc0201"), "field enc0201");
        check(hasField(c, "enc0208"), "field enc0208");
        check(hasField(c, "enc0212"), "field enc0212");
        check(hasField(c, "sgp"), "field sgp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
