/*
 * @test
 * @summary sun.net.www.protocol.http.HttpCallerInfo conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main HttpCallerInfoTest
 */
public class HttpCallerInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.HttpCallerInfo", false, HttpCallerInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "addr"), "field addr");
        check(hasField(c, "authType"), "field authType");
        check(hasField(c, "authenticator"), "field authenticator");
        check(hasField(c, "host"), "field host");
        check(hasField(c, "port"), "field port");
        check(hasField(c, "prompt"), "field prompt");
        check(hasField(c, "protocol"), "field protocol");
        check(hasField(c, "scheme"), "field scheme");
        check(hasField(c, "serverCert"), "field serverCert");
        check(hasField(c, "url"), "field url");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
