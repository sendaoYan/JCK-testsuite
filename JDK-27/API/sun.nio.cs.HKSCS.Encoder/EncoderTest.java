/*
 * @test
 * @summary sun.nio.cs.HKSCS.Encoder conformance: internal package reflection.
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
        Class<?> c = Class.forName("sun.nio.cs.HKSCS$Encoder", false, EncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.DoubleByte$Encoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canEncode"), "method canEncode");
        check(hasMethod(c, "encodeArrayLoop"), "method encodeArrayLoop");
        check(hasMethod(c, "encodeBig5"), "method encodeBig5");
        check(hasMethod(c, "encodeBufferLoop"), "method encodeBufferLoop");
        check(hasMethod(c, "encodeChar"), "method encodeChar");
        check(hasMethod(c, "encodeFromUTF16"), "method encodeFromUTF16");
        check(hasMethod(c, "encodeLoop"), "method encodeLoop");
        check(hasMethod(c, "encodeSupp"), "method encodeSupp");
        check(hasMethod(c, "initc2b"), "method initc2b");
        check(hasField(c, "C2B_UNMAPPABLE"), "field C2B_UNMAPPABLE");
        check(hasField(c, "big5Enc"), "field big5Enc");
        check(hasField(c, "c2bBmp"), "field c2bBmp");
        check(hasField(c, "c2bSupp"), "field c2bSupp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
