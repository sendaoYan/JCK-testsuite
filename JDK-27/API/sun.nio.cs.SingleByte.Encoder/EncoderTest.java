/*
 * @test
 * @summary sun.nio.cs.SingleByte.Encoder conformance: internal package reflection.
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
        Class<?> c = Class.forName("sun.nio.cs.SingleByte$Encoder", false, EncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.nio.cs.ArrayEncoder"), "interface sun.nio.cs.ArrayEncoder");
        check(hasMethod(c, "canEncode"), "method canEncode");
        check(hasMethod(c, "encode"), "method encode");
        check(hasMethod(c, "encodeFromLatin1"), "method encodeFromLatin1");
        check(hasMethod(c, "encodeFromUTF16"), "method encodeFromUTF16");
        check(hasMethod(c, "encodeLoop"), "method encodeLoop");
        check(hasMethod(c, "implReplaceWith"), "method implReplaceWith");
        check(hasMethod(c, "isASCIICompatible"), "method isASCIICompatible");
        check(hasMethod(c, "isLegalReplacement"), "method isLegalReplacement");
        check(hasField(c, "c2b"), "field c2b");
        check(hasField(c, "c2bIndex"), "field c2bIndex");
        check(hasField(c, "isASCIICompatible"), "field isASCIICompatible");
        check(hasField(c, "repl"), "field repl");
        check(hasField(c, "sgp"), "field sgp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
