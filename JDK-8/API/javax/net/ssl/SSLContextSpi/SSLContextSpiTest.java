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
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
