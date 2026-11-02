/*
 * @test
 * @summary sun.nio.cs.DoubleByte.Decoder conformance: internal package reflection.
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
        Class<?> c = Class.forName("sun.nio.cs.DoubleByte$Decoder", false, DecoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "sun.nio.cs.ArrayDecoder"), "interface sun.nio.cs.ArrayDecoder");
        check(hasInterface(c, "sun.nio.cs.DelegatableDecoder"), "interface sun.nio.cs.DelegatableDecoder");
        check(hasMethod(c, "crMalformedOrUnderFlow"), "method crMalformedOrUnderFlow");
        check(hasMethod(c, "crMalformedOrUnmappable"), "method crMalformedOrUnmappable");
        check(hasMethod(c, "decode"), "method decode");
        check(hasMethod(c, "decodeArrayLoop"), "method decodeArrayLoop");
        check(hasMethod(c, "decodeBufferLoop"), "method decodeBufferLoop");
        check(hasMethod(c, "decodeDouble"), "method decodeDouble");
        check(hasMethod(c, "decodeLoop"), "method decodeLoop");
        check(hasMethod(c, "decodeSingle"), "method decodeSingle");
        check(hasMethod(c, "implFlush"), "method implFlush");
        check(hasMethod(c, "implReset"), "method implReset");
        check(hasMethod(c, "isASCIICompatible"), "method isASCIICompatible");
        check(hasField(c, "b2Max"), "field b2Max");
        check(hasField(c, "b2Min"), "field b2Min");
        check(hasField(c, "b2c"), "field b2c");
        check(hasField(c, "b2cSB"), "field b2cSB");
        check(hasField(c, "isASCIICompatible"), "field isASCIICompatible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
