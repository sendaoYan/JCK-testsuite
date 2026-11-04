/*
 * @test
 * @summary javax.net.ssl.SSLServerSocketFactory conformance: white-box structure + black-box behavior (unbound semantics).
 * @run main SSLServerSocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLServerSocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == javax.net.ServerSocketFactory.class, "superclass javax.net.ServerSocketFactory");
        String[] names = {"getDefault", "getDefaultCipherSuites", "getSupportedCipherSuites", "createServerSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLServerSocketFactory def = (javax.net.ssl.SSLServerSocketFactory)
                javax.net.ssl.SSLServerSocketFactory.getDefault();
        check(def != null, "default non-null");
        check(def.getDefaultCipherSuites().length > 0, "default ciphers non-empty");
        check(def.getSupportedCipherSuites().length > 0, "supported ciphers non-empty");
        // unbound server socket creation only: no port binding, no accept
        javax.net.ssl.SSLServerSocket ss = (javax.net.ssl.SSLServerSocket) def.createServerSocket();
        check(ss != null && !ss.isBound(), "createServerSocket() unbound");
        ss.close();

        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        javax.net.ssl.SSLServerSocketFactory sf = ctx.getServerSocketFactory();
        check(sf instanceof javax.net.ssl.SSLServerSocketFactory, "context server factory type");
        javax.net.ssl.SSLServerSocket ss2 = (javax.net.ssl.SSLServerSocket) sf.createServerSocket();
        check(ss2 != null && !ss2.isBound(), "context factory createServerSocket unbound");
        ss2.close();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
