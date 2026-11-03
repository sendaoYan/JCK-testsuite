/*
 * @test
 * @summary javax.naming.event.NamingListener conformance: white-box structure + black-box behavior.
 * @run main NamingListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.EventListener;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;

public class NamingListenerTest {
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
        Class<?> c = Class.forName("javax.naming.event.NamingListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean ext = false;
        for (Class<?> itf : c.getInterfaces())
            if (itf == EventListener.class) ext = true;
        check(ext, "extends java.util.EventListener");

        check(hasMethod(c, "namingExceptionThrown", NamingExceptionEvent.class),
                "namingExceptionThrown(NamingExceptionEvent)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Recorder r = new Recorder();
        EventContext src = eventContext();
        NamingException exc = new NamingException("x");
        NamingExceptionEvent evt = new NamingExceptionEvent(src, exc);

        // Direct invocation.
        r.namingExceptionThrown(evt);
        check(r.received == evt, "direct namingExceptionThrown callable");

        // NamingExceptionEvent.dispatch targets namingExceptionThrown on the listener.
        r.received = null;
        evt.dispatch(r);
        check(r.received == evt, "NamingExceptionEvent.dispatch invokes namingExceptionThrown");

        // The listener is usable as a plain EventListener reference.
        EventListener asEventListener = r;
        check(asEventListener instanceof NamingListener, "usable as EventListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
