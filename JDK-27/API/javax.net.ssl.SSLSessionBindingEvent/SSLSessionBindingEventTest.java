/*
 * @test
 * @summary javax.net.ssl.SSLSessionBindingEvent conformance: white-box structure + black-box behavior.
 * @run main SSLSessionBindingEventTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSessionBindingEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionBindingEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Class.forName("javax.net.ssl.SSLSessionBindingEvent").getSuperclass() == Class.forName("java.util.EventObject"), "superclass java.util.EventObject");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLSession dummy = (javax.net.ssl.SSLSession) java.lang.reflect.Proxy.newProxyInstance(
            javax.net.ssl.SSLSession.class.getClassLoader(), new Class[]{javax.net.ssl.SSLSession.class},
            (p, m, a) -> null);
        javax.net.ssl.SSLSessionBindingEvent ev = new javax.net.ssl.SSLSessionBindingEvent(dummy, "attr");
        check(ev.getName().equals("attr"), "name");
        check(ev.getSession() == dummy, "session");
        check(ev.getSource() == dummy, "source is session");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
