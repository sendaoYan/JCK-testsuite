/*
 * @test
 * @summary javax.net.ServerSocketFactory conformance: white-box structure + black-box behavior (unbound semantics).
 * @run main ServerSocketFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.net.*;
public class ServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ServerSocketFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0 && Modifier.isProtected(k.getModifiers())) protCtor = true;
        check(protCtor, "protected no-arg constructor");
        String[] names = {"getDefault", "createServerSocket"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ServerSocketFactory def = javax.net.ServerSocketFactory.getDefault();
        check(def != null, "default non-null");
        // createServerSocket() returns an unbound server socket (no port binding)
        java.net.ServerSocket ss = def.createServerSocket();
        check(ss != null && !ss.isBound(), "createServerSocket() unbound");
        ss.close();
        // subclass factory: the no-arg createServerSocket() is inherited from the base class,
        // which does not implement it (throws SocketException).
        class F extends javax.net.ServerSocketFactory {
            public java.net.ServerSocket createServerSocket(int p) throws java.io.IOException { return new java.net.ServerSocket(); }
            public java.net.ServerSocket createServerSocket(int p, int b) throws java.io.IOException { return new java.net.ServerSocket(); }
            public java.net.ServerSocket createServerSocket(int p, int b, java.net.InetAddress a) throws java.io.IOException { return new java.net.ServerSocket(); }
        }
        F f = new F();
        check(expect(() -> { f.createServerSocket(); return "ok"; }).equals("SocketException"),
                "base no-arg createServerSocket() throws SocketException for a subclass that does not override it");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
