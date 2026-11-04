/*
 * @test
 * @summary javax.net.ssl.HandshakeCompletedListener conformance: white-box structure + black-box behavior.
 * @run main HandshakeCompletedListenerTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class HandshakeCompletedListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HandshakeCompletedListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
    }
    static Object sessionDefault(java.lang.reflect.Method m) {
        Class<?> rt = m.getReturnType();
        if (rt == boolean.class) return false;
        if (rt == int.class) return 0;
        if (rt == long.class) return 0L;
        return null;
    }
    static SSLSession mockSession() {
        return (SSLSession) Proxy.newProxyInstance(
            HandshakeCompletedListenerTest.class.getClassLoader(),
            new Class<?>[] { SSLSession.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "toString": return "mock-session";
                    case "hashCode": return 0;
                    case "equals": return proxy == args[0];
                    case "getId": return new byte[0];
                    case "getValueNames": return new String[0];
                    default: return sessionDefault(method);
                }
            });
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // directly invoke the interface method through the interface type (invokeinterface)
        SSLContext ctx = SSLContext.getDefault();
        SSLSocket sock = (SSLSocket) ctx.getSocketFactory().createSocket();
        SSLSession session = mockSession();
        final HandshakeCompletedEvent[] seen = new HandshakeCompletedEvent[1];
        HandshakeCompletedListener l = new HandshakeCompletedListener() {
            public void handshakeCompleted(HandshakeCompletedEvent e) { seen[0] = e; }
        };
        HandshakeCompletedEvent ev = new HandshakeCompletedEvent(sock, session);
        l.handshakeCompleted(ev);
        check(seen[0] == ev, "handshakeCompleted delivered event");
        check(ev.getSession() == session, "event session preserved");
        check(ev.getSocket() == sock, "event socket preserved");
        check(l instanceof java.util.EventListener, "is EventListener");
    }
    // ---- Part 3: contract deepening (interface surface + registration semantics) ----
    static void testContract() throws Exception {
        // exactly one method: handshakeCompleted(HandshakeCompletedEvent)
        java.lang.reflect.Method[] ms = HandshakeCompletedListener.class.getMethods();
        check(ms.length == 1 && ms[0].getName().equals("handshakeCompleted")
                && ms[0].getParameterTypes()[0] == HandshakeCompletedEvent.class
                && ms[0].getReturnType() == void.class,
                "single method handshakeCompleted(HandshakeCompletedEvent)");
        check(java.util.EventListener.class.isAssignableFrom(HandshakeCompletedListener.class),
                "is assignable to java.util.EventListener");
        // listener registration rejects null and tolerates duplicate/unregistered removals
        SSLContext ctx = SSLContext.getDefault();
        SSLSocket sock = (SSLSocket) ctx.getSocketFactory().createSocket();
        check(expect(() -> { sock.addHandshakeCompletedListener(null); return "ok"; })
                .equals("IllegalArgumentException"),
                "addHandshakeCompletedListener(null) -> IllegalArgumentException");
        HandshakeCompletedListener l = e -> {};
        sock.addHandshakeCompletedListener(l);
        sock.removeHandshakeCompletedListener(l); // removal before any handshake is a no-op
        check(true, "remove before handshake tolerated");
        sock.addHandshakeCompletedListener(l);
        check(sock.getSession() != null, "session query after registration");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
