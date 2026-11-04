/*
 * @test
 * @summary sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.https:+open
 * @run main AbstractDelegateHttpsURLConnectionTest
 */
public class AbstractDelegateHttpsURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection", false, AbstractDelegateHttpsURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.HttpURLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "disconnect"), "method disconnect");
        check(hasMethod(c, "getCipherSuite"), "method getCipherSuite");
        check(hasMethod(c, "getHostnameVerifier"), "method getHostnameVerifier");
        check(hasMethod(c, "getHttpCallerInfo"), "method getHttpCallerInfo");
        check(hasMethod(c, "getLocalCertificates"), "method getLocalCertificates");
        check(hasMethod(c, "getNewHttpClient"), "method getNewHttpClient");
        check(hasMethod(c, "getSSLSocketFactory"), "method getSSLSocketFactory");
        check(hasMethod(c, "getServerCertificates"), "method getServerCertificates");
        check(hasMethod(c, "isConnected"), "method isConnected");
        check(hasMethod(c, "noResponseBody"), "method noResponseBody");
        check(hasMethod(c, "proxiedConnect"), "method proxiedConnect");
        check(hasMethod(c, "setConnected"), "method setConnected");
        check(hasMethod(c, "setNewClient"), "method setNewClient");
        check(hasMethod(c, "setProxiedClient"), "method setProxiedClient");
        check(hasField(c, "savedSession"), "field savedSession");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
