/*
 * @test
 * @summary jdk.internal.net.quic.QuicTLSEngine.HandshakeState conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main HandshakeStateTest
 */
public class HandshakeStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicTLSEngine$HandshakeState", false, HandshakeStateTest.class.getClassLoader());
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
        check(hasField(c, "HANDSHAKE_CONFIRMED"), "field HANDSHAKE_CONFIRMED");
        check(hasField(c, "NEED_RECV_CRYPTO"), "field NEED_RECV_CRYPTO");
        check(hasField(c, "NEED_RECV_HANDSHAKE_DONE"), "field NEED_RECV_HANDSHAKE_DONE");
        check(hasField(c, "NEED_SEND_CRYPTO"), "field NEED_SEND_CRYPTO");
        check(hasField(c, "NEED_SEND_HANDSHAKE_DONE"), "field NEED_SEND_HANDSHAKE_DONE");
        check(hasField(c, "NEED_TASK"), "field NEED_TASK");
        check(hasEnumConstant(c, "HANDSHAKE_CONFIRMED"), "enum HANDSHAKE_CONFIRMED");
        check(hasEnumConstant(c, "NEED_RECV_CRYPTO"), "enum NEED_RECV_CRYPTO");
        check(hasEnumConstant(c, "NEED_RECV_HANDSHAKE_DONE"), "enum NEED_RECV_HANDSHAKE_DONE");
        check(hasEnumConstant(c, "NEED_SEND_CRYPTO"), "enum NEED_SEND_CRYPTO");
        check(hasEnumConstant(c, "NEED_SEND_HANDSHAKE_DONE"), "enum NEED_SEND_HANDSHAKE_DONE");
        check(hasEnumConstant(c, "NEED_TASK"), "enum NEED_TASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
