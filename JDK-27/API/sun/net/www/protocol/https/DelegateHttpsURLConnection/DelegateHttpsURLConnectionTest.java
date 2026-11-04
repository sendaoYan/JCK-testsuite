/*
 * @test
 * @summary sun.net.www.protocol.https.DelegateHttpsURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.https:+open
 * @run main DelegateHttpsURLConnectionTest
 */
public class DelegateHttpsURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.https.DelegateHttpsURLConnection", false, DelegateHttpsURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getHostnameVerifier"), "method getHostnameVerifier");
        check(hasMethod(c, "getSSLSocketFactory"), "method getSSLSocketFactory");
        check(hasField(c, "httpsURLConnection"), "field httpsURLConnection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
