/*
 * @test
 * @summary sun.net.NetworkClient conformance: internal package reflection.
 * @modules java.base/sun.net:+open
 * @run main NetworkClientTest
 */
public class NetworkClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.NetworkClient", false, NetworkClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "closeServer"), "method closeServer");
        check(hasMethod(c, "createSocket"), "method createSocket");
        check(hasMethod(c, "doConnect"), "method doConnect");
        check(hasMethod(c, "getConnectTimeout"), "method getConnectTimeout");
        check(hasMethod(c, "getLocalAddress"), "method getLocalAddress");
        check(hasMethod(c, "getReadTimeout"), "method getReadTimeout");
        check(hasMethod(c, "openServer"), "method openServer");
        check(hasMethod(c, "serverIsOpen"), "method serverIsOpen");
        check(hasMethod(c, "setConnectTimeout"), "method setConnectTimeout");
        check(hasMethod(c, "setReadTimeout"), "method setReadTimeout");
        check(hasField(c, "DEFAULT_CONNECT_TIMEOUT"), "field DEFAULT_CONNECT_TIMEOUT");
        check(hasField(c, "DEFAULT_READ_TIMEOUT"), "field DEFAULT_READ_TIMEOUT");
        check(hasField(c, "connectTimeout"), "field connectTimeout");
        check(hasField(c, "defaultConnectTimeout"), "field defaultConnectTimeout");
        check(hasField(c, "defaultSoTimeout"), "field defaultSoTimeout");
        check(hasField(c, "encoding"), "field encoding");
        check(hasField(c, "proxy"), "field proxy");
        check(hasField(c, "readTimeout"), "field readTimeout");
        check(hasField(c, "serverInput"), "field serverInput");
        check(hasField(c, "serverOutput"), "field serverOutput");
        check(hasField(c, "serverSocket"), "field serverSocket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
