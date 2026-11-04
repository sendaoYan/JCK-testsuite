/*
 * @test
 * @summary javax.net.ssl.HostnameVerifier conformance: white-box structure + black-box behavior.
 * @run main HostnameVerifierTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class HostnameVerifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HostnameVerifier");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.HostnameVerifier v = (h, s) -> h.equals("good");
        check(v.verify("good", null), "verify true");
        check(!v.verify("bad", null), "verify false");
    }
    // ---- Part 3: contract deepening (spi defaults + per-connection wiring) ----
    static void testContract() throws Exception {
        // the interface exposes exactly one method
        Method[] ms = HostnameVerifier.class.getMethods();
        check(ms.length == 1 && ms[0].getName().equals("verify")
                && ms[0].getParameterCount() == 2
                && ms[0].getParameterTypes()[0] == String.class
                && ms[0].getParameterTypes()[1] == SSLSession.class
                && ms[0].getReturnType() == boolean.class,
                "single method verify(String, SSLSession) -> boolean");
        check(HostnameVerifier.class.getInterfaces().length == 0
                || java.util.Arrays.asList(HostnameVerifier.class.getInterfaces()).contains(java.util.EventListener.class) == false,
                "standalone interface (not an EventListener)");
        // default verifier installed in HttpsURLConnection: without a real session it never verifies
        HostnameVerifier def = HttpsURLConnection.getDefaultHostnameVerifier();
        check(def != null, "default hostname verifier non-null");
        check(!def.verify("localhost", null), "default verifier rejects a null session");
        check(!def.verify("127.0.0.1", null), "default verifier rejects any host with null session");
        // set/get round trip with restore
        HostnameVerifier prev = HttpsURLConnection.getDefaultHostnameVerifier();
        HostnameVerifier mine = (h, s) -> h.endsWith(".example.com");
        HttpsURLConnection.setDefaultHostnameVerifier(mine);
        check(HttpsURLConnection.getDefaultHostnameVerifier() == mine, "setDefaultHostnameVerifier installed");
        check(HttpsURLConnection.getDefaultHostnameVerifier().verify("a.example.com", null),
                "installed verifier consulted");
        check(!HttpsURLConnection.getDefaultHostnameVerifier().verify("a.example.org", null),
                "installed verifier negative path");
        HttpsURLConnection.setDefaultHostnameVerifier(prev);
        check(HttpsURLConnection.getDefaultHostnameVerifier() == prev, "default verifier restored");
        check(expect(() -> { HttpsURLConnection.setDefaultHostnameVerifier(null); return "ok"; })
                .equals("IllegalArgumentException"), "setDefaultHostnameVerifier(null) -> IllegalArgumentException");
        // per-connection verifier is independent of the process default
        java.net.URL u = new java.net.URL("https://127.0.0.1:1/");
        HttpsURLConnection con = (HttpsURLConnection) u.openConnection();
        con.setHostnameVerifier(mine);
        check(con.getHostnameVerifier() == mine, "per-connection verifier set");
        check(HttpsURLConnection.getDefaultHostnameVerifier() == prev,
                "per-connection verifier does not touch the default");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
