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
    static Object sessionDefault(java.lang.reflect.Method m) {
        Class<?> rt = m.getReturnType();
        if (rt == boolean.class) return false;
        if (rt == int.class) return 0;
        if (rt == long.class) return 0L;
        return null;
    }
    static SSLSession mockSession() {
        return (SSLSession) Proxy.newProxyInstance(
            SSLSessionBindingListenerTest.class.getClassLoader(),
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
        // directly invoke the interface methods through the interface type (invokeinterface)
        SSLSession session = mockSession();
        final String[] bound = new String[1], unbound = new String[1];
        SSLSessionBindingListener l = new SSLSessionBindingListener() {
            public void valueBound(SSLSessionBindingEvent e) { bound[0] = e.getName(); }
            public void valueUnbound(SSLSessionBindingEvent e) { unbound[0] = e.getName(); }
        };
        SSLSessionBindingEvent ev = new SSLSessionBindingEvent(session, "attr");
        l.valueBound(ev);
        l.valueUnbound(ev);
        check("attr".equals(bound[0]), "valueBound delivered name");
        check("attr".equals(unbound[0]), "valueUnbound delivered name");
        check("attr".equals(ev.getName()), "event name");
        check(ev.getSession() == session, "event session preserved");
        check(l instanceof java.util.EventListener, "is EventListener");
    }
    // ---- Part 3: contract deepening (interface surface) ----
    static void testContract() throws Exception {
        // exactly two methods, both void with one SSLSessionBindingEvent parameter
        Method bound = SSLSessionBindingListener.class.getMethod("valueBound", SSLSessionBindingEvent.class);
        Method unbound = SSLSessionBindingListener.class.getMethod("valueUnbound", SSLSessionBindingEvent.class);
        check(bound.getReturnType() == void.class && unbound.getReturnType() == void.class,
                "valueBound/valueUnbound are void");
        check(SSLSessionBindingListener.class.getMethods().length == 2,
                "interface declares exactly 2 methods");
        // EventListener inheritance (extends by declaration)
        check(java.util.EventListener.class.isAssignableFrom(SSLSessionBindingListener.class),
                "is assignable to java.util.EventListener");
        // an anonymous implementation is delivered both callbacks in order
        final java.util.List<String> seq = new java.util.ArrayList<String>();
        SSLSession s = mockSession();
        SSLSessionBindingListener l = new SSLSessionBindingListener() {
            public void valueBound(SSLSessionBindingEvent e) { seq.add("bound:" + e.getName()); }
            public void valueUnbound(SSLSessionBindingEvent e) { seq.add("unbound:" + e.getName()); }
        };
        l.valueBound(new SSLSessionBindingEvent(s, "a"));
        l.valueUnbound(new SSLSessionBindingEvent(s, "a"));
        check(seq.equals(java.util.Arrays.asList("bound:a", "unbound:a")), "callbacks delivered in call order");
        // lambda is impossible (2 abstract methods); assert the interface is not functional
        check(!SSLSessionBindingListener.class.isAnnotationPresent(FunctionalInterface.class),
                "not a FunctionalInterface");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
