/*
 * @test
 * @summary sun.nio.cs.HKSCS.Decoder conformance: internal package reflection.
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
        Class<?> c = Class.forName("sun.nio.cs.HKSCS$Decoder", false, DecoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.DoubleByte$Decoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decode"), "method decode");
        check(hasMethod(c, "decodeArrayLoop"), "method decodeArrayLoop");
        check(hasMethod(c, "decodeBig5"), "method decodeBig5");
        check(hasMethod(c, "decodeBufferLoop"), "method decodeBufferLoop");
        check(hasMethod(c, "decodeDouble"), "method decodeDouble");
        check(hasMethod(c, "decodeDoubleEx"), "method decodeDoubleEx");
        check(hasMethod(c, "decodeLoop"), "method decodeLoop");
        check(hasMethod(c, "decodeSingle"), "method decodeSingle");
        check(hasMethod(c, "initb2c"), "method initb2c");
        check(hasField(c, "b2Max"), "field b2Max");
        check(hasField(c, "b2Min"), "field b2Min");
        check(hasField(c, "b2cBmp"), "field b2cBmp");
        check(hasField(c, "b2cSupp"), "field b2cSupp");
        check(hasField(c, "big5Dec"), "field big5Dec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
