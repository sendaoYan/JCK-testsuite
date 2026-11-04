/*
 * @test
 * @summary javax.net.ssl.HttpsURLConnection conformance: abstract class hierarchy and key methods.
 * @run main HttpsURLConnectionTest
 */

public class HttpsURLConnectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HttpsURLConnection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.net.HttpURLConnection"), "extends java.net.HttpURLConnection");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCipherSuite"), "declares getCipherSuite");
        check(ms.contains("getDefaultHostnameVerifier"), "declares getDefaultHostnameVerifier");
        check(ms.contains("getDefaultSSLSocketFactory"), "declares getDefaultSSLSocketFactory");
        check(ms.contains("getHostnameVerifier"), "declares getHostnameVerifier");
        check(ms.contains("getLocalCertificates"), "declares getLocalCertificates");
        check(ms.contains("getLocalPrincipal"), "declares getLocalPrincipal");
        check(ms.contains("getPeerPrincipal"), "declares getPeerPrincipal");
        check(ms.contains("getSSLSocketFactory"), "declares getSSLSocketFactory");
        check(ms.contains("getServerCertificates"), "declares getServerCertificates");
        check(ms.contains("setDefaultHostnameVerifier"), "declares setDefaultHostnameVerifier");
        check(ms.contains("setDefaultSSLSocketFactory"), "declares setDefaultSSLSocketFactory");
        check(ms.contains("setHostnameVerifier"), "declares setHostnameVerifier");
        check(ms.contains("setSSLSocketFactory"), "declares setSSLSocketFactory");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
