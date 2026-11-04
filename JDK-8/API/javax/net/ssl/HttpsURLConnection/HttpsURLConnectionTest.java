/*
 * @test
 * @summary javax.net.ssl.HttpsURLConnection conformance: white-box structure + black-box behavior (no connection).
 * @run main HttpsURLConnectionTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;
public class HttpsURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HttpsURLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == java.net.HttpURLConnection.class, "superclass java.net.HttpURLConnection");
        String[] names = {"getCipherSuite", "getLocalCertificates", "getServerCertificates",
                "getPeerPrincipal", "getLocalPrincipal", "setHostnameVerifier", "getHostnameVerifier",
                "setSSLSocketFactory", "getSSLSocketFactory", "setDefaultHostnameVerifier",
                "getDefaultHostnameVerifier", "setDefaultSSLSocketFactory", "getDefaultSSLSocketFactory"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default hostname verifier is always installed
        javax.net.ssl.HostnameVerifier prevV = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier();
        check(prevV != null, "default hostname verifier non-null");
        // default SSLSocketFactory is always installed
        javax.net.ssl.SSLSocketFactory prevF = javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory();
        check(prevF != null, "default SSLSocketFactory non-null");

        // roundtrip a fresh factory (no connection made)
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        javax.net.ssl.SSLSocketFactory sf = ctx.getSocketFactory();
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sf);
        check(javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory() == sf, "default SSLSocketFactory set");
        // restore
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(prevF);
        check(javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory() == prevF, "default SSLSocketFactory restored");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
