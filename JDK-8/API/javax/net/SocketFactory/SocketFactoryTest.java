/*
 * @test
 * @summary javax.net.SocketFactory conformance: white-box structure + black-box behavior (unconnected semantics).
 * @run main SocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.net.*;
public class SocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.SocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0 && Modifier.isProtected(k.getModifiers())) protCtor = true;
        check(protCtor, "protected no-arg constructor");
        String[] names = {"getDefault", "createSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.SocketFactory def = javax.net.SocketFactory.getDefault();
        check(def != null, "default non-null");
        // the platform default factory overrides the no-arg createSocket() to yield an unconnected socket
        java.net.Socket s = def.createSocket();
        check(s != null && !s.isConnected(), "default createSocket() unconnected");
        s.close();
        // a minimal subclass inherits the base no-arg createSocket(), which is not implemented
        class F extends javax.net.SocketFactory {
            public java.net.Socket createSocket(String h, int p) { return new java.net.Socket(); }
            public java.net.Socket createSocket(String h, int p, java.net.InetAddress a, int lp) { return new java.net.Socket(); }
            public java.net.Socket createSocket(java.net.InetAddress a, int p) { return new java.net.Socket(); }
            public java.net.Socket createSocket(java.net.InetAddress a, int p, java.net.InetAddress l, int lp) { return new java.net.Socket(); }
        }
        F f = new F();
        check(expect(() -> { f.createSocket(); return "ok"; }).equals("SocketException"),
                "base no-arg createSocket() throws SocketException for a subclass that does not override it");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
