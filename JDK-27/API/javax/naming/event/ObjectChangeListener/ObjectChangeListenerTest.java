/*
 * @test
 * @summary javax.naming.event.ObjectChangeListener conformance: white-box structure + black-box behavior.
 * @run main ObjectChangeListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.Binding;
import javax.naming.event.EventContext;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;

public class ObjectChangeListenerTest {
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

    static class Recorder implements ObjectChangeListener {
        NamingEvent changed;
        NamingExceptionEvent exception;
        public void objectChanged(NamingEvent evt) { changed = evt; }
        public void namingExceptionThrown(NamingExceptionEvent evt) { exception = evt; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.event.ObjectChangeListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean ext = false;
        for (Class<?> itf : c.getInterfaces())
            if (itf.getName().equals("javax.naming.event.NamingListener")) ext = true;
        check(ext, "extends NamingListener");

        check(hasMethod(c, "objectChanged", NamingEvent.class), "objectChanged(NamingEvent)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        EventContext src = eventContext();
        Recorder r = new Recorder();

        // NamingEvent.OBJECT_CHANGED dispatches to objectChanged.
        NamingEvent changed = new NamingEvent(src, NamingEvent.OBJECT_CHANGED,
                new Binding("n", "new"), new Binding("n", "old"), "diff");
        changed.dispatch(r);
        check(r.changed == changed, "OBJECT_CHANGED dispatch -> objectChanged");

        // Direct invocation is also callable.
        r.changed = null;
        r.objectChanged(changed);
        check(r.changed == changed, "direct objectChanged callable");

        // Inherited namingExceptionThrown is callable.
        r.namingExceptionThrown(null);
        check(r.exception == null, "namingExceptionThrown callable (inherited)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
