/*
 * @test
 * @summary javax.net.ssl.HandshakeCompletedEvent conformance: white-box structure + black-box behavior (no real handshake).
 * @run main HandshakeCompletedEventTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;
public class HandshakeCompletedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HandshakeCompletedEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.util.EventObject.class, "superclass java.util.EventObject");
        String[] names = {"getSession", "getCipherSuite", "getLocalCertificates", "getPeerCertificates",
                "getPeerCertificateChain", "getPeerPrincipal", "getLocalPrincipal", "getSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static Object sessionDefault(java.lang.reflect.Method m) {
        Class<?> rt = m.getReturnType();
        if (rt == boolean.class) return false;
        if (rt == int.class) return 0;
        if (rt == long.class) return 0L;
        return null;
    }
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLSocket sock = (javax.net.ssl.SSLSocket)
                javax.net.ssl.SSLSocketFactory.getDefault().createSocket();
        javax.net.ssl.SSLSession sess = (javax.net.ssl.SSLSession) Proxy.newProxyInstance(
            javax.net.ssl.SSLSession.class.getClassLoader(),
            new Class<?>[] { javax.net.ssl.SSLSession.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "toString": return "mock-session";
                    case "hashCode": return 0;
                    case "equals": return proxy == args[0];
                    default: return sessionDefault(method);
                }
            });
        javax.net.ssl.HandshakeCompletedEvent ev = new javax.net.ssl.HandshakeCompletedEvent(sock, sess);
        check(ev.getSocket() == sock, "socket");
        check(ev.getSession() == sess, "session");
        check(ev.getSource() == sock, "source is socket");
        check(ev.getCipherSuite() == null, "cipher suite null for mock session");
        // JDK-8 delegates these getters straight to the (mock) session; with a null-returning
        // session they return null rather than throwing (SSLPeerUnverifiedException is only
        // raised by a real session with an empty peer certificate chain).
        check(ev.getPeerCertificates() == null, "peer certs null for mock session");
        check(ev.getPeerCertificateChain() == null, "peer cert chain null for mock session");
        check(ev.getPeerPrincipal() == null, "peer principal null for mock session");
        sock.close();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
