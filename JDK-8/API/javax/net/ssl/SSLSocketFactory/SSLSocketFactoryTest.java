/*
 * @test
 * @summary javax.net.ssl.SSLSocketFactory conformance: white-box structure + black-box behavior (unconnected semantics).
 * @run main SSLSocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == javax.net.SocketFactory.class, "superclass javax.net.SocketFactory");
        String[] names = {"getDefault", "getDefaultCipherSuites", "getSupportedCipherSuites", "createSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLSocketFactory def = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
        check(def != null, "default non-null");
        check(def.getDefaultCipherSuites().length > 0, "default ciphers non-empty");
        check(def.getSupportedCipherSuites().length > 0, "supported ciphers non-empty");
        // unconnected socket creation only: no network, no handshake
        javax.net.ssl.SSLSocket s = (javax.net.ssl.SSLSocket) def.createSocket();
        check(s != null && !s.isConnected(), "createSocket() unconnected");
        s.close();

        // a context's factory is typed correctly and produces unconnected sockets
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        javax.net.ssl.SSLSocketFactory sf = ctx.getSocketFactory();
        check(sf instanceof javax.net.ssl.SSLSocketFactory, "context factory type");
        javax.net.ssl.SSLSocket s2 = (javax.net.ssl.SSLSocket) sf.createSocket();
        check(s2 != null && !s2.isConnected(), "context factory createSocket unconnected");
        s2.close();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
