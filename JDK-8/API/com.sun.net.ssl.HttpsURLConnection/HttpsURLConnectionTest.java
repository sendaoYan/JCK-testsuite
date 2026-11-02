/*
 * @test
 * @summary com.sun.net.ssl.HttpsURLConnection conformance: hierarchy and declared members via reflection.
 * @run main HttpsURLConnectionTest
 */

public class HttpsURLConnectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.HttpsURLConnection", false, null);
        check(c.getName().equals("com.sun.net.ssl.HttpsURLConnection"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.HttpURLConnection"), "superclass java.net.HttpURLConnection");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCipherSuite"), "declares getCipherSuite");
        check(ms.contains("getDefaultHostnameVerifier"), "declares getDefaultHostnameVerifier");
        check(ms.contains("getDefaultSSLSocketFactory"), "declares getDefaultSSLSocketFactory");
        check(ms.contains("getHostnameVerifier"), "declares getHostnameVerifier");
        check(ms.contains("getSSLSocketFactory"), "declares getSSLSocketFactory");
        check(ms.contains("getServerCertificateChain"), "declares getServerCertificateChain");
        check(ms.contains("setDefaultHostnameVerifier"), "declares setDefaultHostnameVerifier");
        check(ms.contains("setDefaultSSLSocketFactory"), "declares setDefaultSSLSocketFactory");
        check(ms.contains("setHostnameVerifier"), "declares setHostnameVerifier");
        check(ms.contains("setSSLSocketFactory"), "declares setSSLSocketFactory");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
