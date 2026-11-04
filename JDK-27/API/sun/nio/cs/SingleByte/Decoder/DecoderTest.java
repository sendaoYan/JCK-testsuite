/*
 * @test
 * @summary sun.nio.cs.SingleByte.Decoder conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main DecoderTest
 */
public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.SingleByte$Decoder", false, DecoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.nio.cs.ArrayDecoder"), "interface sun.nio.cs.ArrayDecoder");
        check(hasMethod(c, "decode"), "method decode");
        check(hasMethod(c, "decodeLoop"), "method decodeLoop");
        check(hasMethod(c, "decodeToLatin1"), "method decodeToLatin1");
        check(hasMethod(c, "implReplaceWith"), "method implReplaceWith");
        check(hasMethod(c, "isASCIICompatible"), "method isASCIICompatible");
        check(hasMethod(c, "isLatin1Decodable"), "method isLatin1Decodable");
        check(hasField(c, "b2c"), "field b2c");
        check(hasField(c, "isASCIICompatible"), "field isASCIICompatible");
        check(hasField(c, "isLatin1Decodable"), "field isLatin1Decodable");
        check(hasField(c, "repl"), "field repl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
