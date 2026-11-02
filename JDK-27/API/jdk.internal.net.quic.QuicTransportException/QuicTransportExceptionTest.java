/*
 * @test
 * @summary jdk.internal.net.quic.QuicTransportException conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main QuicTransportExceptionTest
 */
public class QuicTransportExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicTransportException", false, QuicTransportExceptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getErrorCode"), "method getErrorCode");
        check(hasMethod(c, "getFrameType"), "method getFrameType");
        check(hasMethod(c, "getKeySpace"), "method getKeySpace");
        check(hasMethod(c, "getReason"), "method getReason");
        check(hasField(c, "errorCode"), "field errorCode");
        check(hasField(c, "frameType"), "field frameType");
        check(hasField(c, "keySpace"), "field keySpace");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
