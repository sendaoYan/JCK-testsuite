/*
 * @test
 * @summary jdk.internal.net.quic.QuicTLSEngine.KeySpace conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main KeySpaceTest
 */
public class KeySpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicTLSEngine$KeySpace", false, KeySpaceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "HANDSHAKE"), "field HANDSHAKE");
        check(hasField(c, "INITIAL"), "field INITIAL");
        check(hasField(c, "ONE_RTT"), "field ONE_RTT");
        check(hasField(c, "RETRY"), "field RETRY");
        check(hasField(c, "ZERO_RTT"), "field ZERO_RTT");
        check(hasEnumConstant(c, "HANDSHAKE"), "enum HANDSHAKE");
        check(hasEnumConstant(c, "INITIAL"), "enum INITIAL");
        check(hasEnumConstant(c, "ONE_RTT"), "enum ONE_RTT");
        check(hasEnumConstant(c, "RETRY"), "enum RETRY");
        check(hasEnumConstant(c, "ZERO_RTT"), "enum ZERO_RTT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
