/*
 * @test
 * @summary javax.naming.event.NamingExceptionEvent conformance: white-box structure + black-box behavior.
 * @run main NamingExceptionEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;

public class NamingExceptionEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static EventContext eventContext() {
        return (EventContext) Proxy.newProxyInstance(
                EventContext.class.getClassLoader(),
                new Class<?>[]{EventContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "targetMustExist": return false;
                        case "toString": return "TestEventContext";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return null;
                    }
                });
    }

    static class Recorder implements NamingListener {
        NamingExceptionEvent received;
        public void namingExceptionThrown(NamingExceptionEvent evt) { received = evt; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.event.NamingExceptionEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends java.util.EventObject");
        check(hasMethod(c, "getException"), "getException");
        check(hasMethod(c, "getEventContext"), "getEventContext");
        check(hasMethod(c, "dispatch", NamingListener.class), "dispatch(NamingListener)");
        check(c.getConstructor(EventContext.class, NamingException.class) != null,
                "public constructor (EventContext,NamingException)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        EventContext src = eventContext();
        NamingException exc = new NamingException("boom");

        NamingExceptionEvent evt = new NamingExceptionEvent(src, exc);
        check(evt.getException() == exc, "getException identity");
        check(evt.getEventContext() == src, "getEventContext returns source");
        check(evt.getSource() == src, "getSource returns source");

        Recorder r = new Recorder();
        evt.dispatch(r);
        check(r.received == evt, "dispatch delivers same event to namingExceptionThrown");

        // A second event with a distinct exception is independent of the first.
        NamingException other = new NamingException("second");
        NamingExceptionEvent evt2 = new NamingExceptionEvent(src, other);
        check(evt2.getException() == other, "second event exception identity");
        check(evt.getException() == exc, "first event exception unchanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
