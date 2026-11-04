/*
 * @test
 * @summary javax.net.ssl.SSLContextSpi conformance: white-box structure + black-box behavior.
 * @run main SSLContextSpiTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLContextSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLContextSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class Probe extends javax.net.ssl.SSLContextSpi {
            protected void engineInit(javax.net.ssl.KeyManager[] a, javax.net.ssl.TrustManager[] b, java.security.SecureRandom c) {}
            protected javax.net.ssl.SSLSocketFactory engineGetSocketFactory() { return null; }
            protected javax.net.ssl.SSLServerSocketFactory engineGetServerSocketFactory() { return null; }
            protected javax.net.ssl.SSLEngine engineCreateSSLEngine() { return null; }
            protected javax.net.ssl.SSLEngine engineCreateSSLEngine(String h, int p) { return null; }
            protected javax.net.ssl.SSLSessionContext engineGetServerSessionContext() { return null; }
            protected javax.net.ssl.SSLSessionContext engineGetClientSessionContext() { return null; }
        }
        check(new Probe() != null, "subclassable");
    }
    // ---- Part 3: contract deepening (spi surface + invocation) ----
    static void testContract() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLContextSpi");
        // exactly one public no-arg constructor
        boolean pubNoArg = false; int ctorCount = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            ctorCount++;
            if (k.getParameterCount() == 0 && Modifier.isPublic(k.getModifiers())) pubNoArg = true;
        }
        check(pubNoArg && ctorCount == 1, "single public no-arg constructor");
        // exactly 7 protected abstract engine methods
        String[][] expected = {{"engineInit", "3"}, {"engineGetSocketFactory", "0"},
                {"engineGetServerSocketFactory", "0"}, {"engineCreateSSLEngine", "0"},
                {"engineCreateSSLEngine", "2"}, {"engineGetServerSessionContext", "0"},
                {"engineGetClientSessionContext", "0"}};
        for (String[] e : expected) {
            Method m = c.getDeclaredMethod(e[0], e[1].equals("3")
                    ? new Class<?>[]{KeyManager[].class, TrustManager[].class, java.security.SecureRandom.class}
                    : e[1].equals("2") ? new Class<?>[]{String.class, int.class} : new Class<?>[0]);
            check(Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers()),
                    "protected abstract " + e[0] + "(" + e[1] + " args)");
        }
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 7, "exactly 7 abstract methods, found " + abstractCount);
        // a probe implementation can drive the whole spi surface and record invocations
        final SSLSocketFactory sfSentinel = (SSLSocketFactory) SSLSocketFactory.getDefault();
        final SSLServerSocketFactory ssfSentinel = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        final SSLContext defCtx = SSLContext.getDefault();
        final SSLEngine engSentinel = defCtx.createSSLEngine();
        class Probe extends SSLContextSpi {
            int initCalls;
            KeyManager[] kms; TrustManager[] tms;
            protected void engineInit(KeyManager[] a, TrustManager[] b, java.security.SecureRandom r) {
                initCalls++; kms = a; tms = b;
            }
            protected SSLSocketFactory engineGetSocketFactory() { return sfSentinel; }
            protected SSLServerSocketFactory engineGetServerSocketFactory() { return ssfSentinel; }
            protected SSLEngine engineCreateSSLEngine() { return engSentinel; }
            protected SSLEngine engineCreateSSLEngine(String h, int p) { return engSentinel; }
            protected SSLSessionContext engineGetServerSessionContext() { return defCtx.getServerSessionContext(); }
            protected SSLSessionContext engineGetClientSessionContext() { return defCtx.getClientSessionContext(); }
        }
        Probe p = new Probe();
        p.engineInit(null, null, null);
        check(p.initCalls == 1, "engineInit recorded");
        check(p.engineGetSocketFactory() == sfSentinel, "engineGetSocketFactory returns implementation value");
        check(p.engineGetServerSocketFactory() == ssfSentinel, "engineGetServerSocketFactory returns implementation value");
        check(p.engineCreateSSLEngine() == engSentinel && p.engineCreateSSLEngine("h", 1) == engSentinel,
                "engineCreateSSLEngine overloads return implementation value");
        check(p.engineGetServerSessionContext() != null && p.engineGetClientSessionContext() != null,
                "session context accessors return implementation value");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
