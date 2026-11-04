/*
 * @test
 * @summary sun.nio.cs.DoubleByte.Encoder_EBCDIC conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main Encoder_EBCDICTest
 */
public class Encoder_EBCDICTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.DoubleByte$Encoder_EBCDIC", false, Encoder_EBCDICTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.DoubleByte$Encoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "encodeArrayLoop"), "method encodeArrayLoop");
        check(hasMethod(c, "encodeBufferLoop"), "method encodeBufferLoop");
        check(hasMethod(c, "encodeFromLatin1"), "method encodeFromLatin1");
        check(hasMethod(c, "encodeFromUTF16"), "method encodeFromUTF16");
        check(hasMethod(c, "implFlush"), "method implFlush");
        check(hasMethod(c, "implReset"), "method implReset");
        check(hasField(c, "DBCS"), "field DBCS");
        check(hasField(c, "SBCS"), "field SBCS");
        check(hasField(c, "SI"), "field SI");
        check(hasField(c, "SO"), "field SO");
        check(hasField(c, "currentState"), "field currentState");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
