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
    // ---- Part 3: contract deepening (construction matrix) ----
    static void testContract() throws Exception {
        SSLSession dummy = (SSLSession) Proxy.newProxyInstance(
            SSLSession.class.getClassLoader(), new Class<?>[]{SSLSession.class},
            (p, m, a) -> "toString".equals(m.getName()) ? "mock-session" : null);
        // null session is rejected (EventObject requires a non-null source)
        check(expect(() -> { new SSLSessionBindingEvent(null, "x"); return "ok"; })
                .equals("IllegalArgumentException"), "null session -> IllegalArgumentException");
        // a null name is accepted
        SSLSessionBindingEvent nullName = new SSLSessionBindingEvent(dummy, null);
        check(nullName.getName() == null, "null name preserved");
        check(nullName.getSession() == dummy, "session preserved with null name");
        // declared method surface: exactly getName + getSession
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : SSLSessionBindingEvent.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getName") && names.contains("getSession"), "declares getName/getSession");
        // serialization contract: the event is serializable like its EventObject parent
        check(new SSLSessionBindingEvent(dummy, "x") instanceof java.io.Serializable,
                "SSLSessionBindingEvent is Serializable");
        // toString follows the EventObject convention
        String ts = new SSLSessionBindingEvent(dummy, "x").toString();
        check(ts.startsWith("javax.net.ssl.SSLSessionBindingEvent[") && ts.contains("mock-session"),
                "toString follows EventObject convention: " + ts);
        // EventObject equality is identity based
        SSLSessionBindingEvent e1 = new SSLSessionBindingEvent(dummy, "x");
        check(e1.equals(e1) && !e1.equals(new SSLSessionBindingEvent(dummy, "x")),
                "identity equality from EventObject");
        check(e1.hashCode() == System.identityHashCode(e1), "identity hashCode from EventObject");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
