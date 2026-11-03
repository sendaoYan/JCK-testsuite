/*
 * @test
 * @summary javax.net.ssl.SSLSessionBindingListener conformance: white-box structure + black-box behavior.
 * @run main SSLSessionBindingListenerTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSessionBindingListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionBindingListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class L implements javax.net.ssl.SSLSessionBindingListener {
            String bound, unbound;
            public void valueBound(javax.net.ssl.SSLSessionBindingEvent e) { bound = e.getName(); }
            public void valueUnbound(javax.net.ssl.SSLSessionBindingEvent e) { unbound = e.getName(); }
        }
        check(new L() instanceof java.util.EventListener, "is EventListener");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
