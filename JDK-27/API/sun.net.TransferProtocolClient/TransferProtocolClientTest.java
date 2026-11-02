/*
 * @test
 * @summary sun.net.TransferProtocolClient conformance: internal package reflection.
 * @modules java.base/sun.net:+open
 * @run main TransferProtocolClientTest
 */
public class TransferProtocolClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.TransferProtocolClient", false, TransferProtocolClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.NetworkClient"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getResponseString"), "method getResponseString");
        check(hasMethod(c, "getResponseStrings"), "method getResponseStrings");
        check(hasMethod(c, "readServerResponse"), "method readServerResponse");
        check(hasMethod(c, "sendServer"), "method sendServer");
        check(hasField(c, "debug"), "field debug");
        check(hasField(c, "lastReplyCode"), "field lastReplyCode");
        check(hasField(c, "serverResponse"), "field serverResponse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
