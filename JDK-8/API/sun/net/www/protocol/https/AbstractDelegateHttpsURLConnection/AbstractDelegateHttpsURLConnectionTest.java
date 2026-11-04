/*
 * @test
 * @summary sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection conformance: abstract class and members.
 * @run main AbstractDelegateHttpsURLConnectionTest
 */

public class AbstractDelegateHttpsURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.HttpURLConnection"), "superclass sun.net.www.protocol.http.HttpURLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("connect"), "declares method connect");
        check(mnames.contains("getCipherSuite"), "declares method getCipherSuite");
        check(mnames.contains("getHostnameVerifier"), "declares method getHostnameVerifier");
        check(mnames.contains("getLocalCertificates"), "declares method getLocalCertificates");
        check(mnames.contains("getLocalPrincipal"), "declares method getLocalPrincipal");
        check(mnames.contains("getNewHttpClient"), "declares method getNewHttpClient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
