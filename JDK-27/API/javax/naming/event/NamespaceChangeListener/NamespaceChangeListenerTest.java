/*
 * @test
 * @summary javax.naming.event.NamespaceChangeListener conformance: white-box structure + black-box behavior.
 * @run main NamespaceChangeListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.Binding;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;

public class NamespaceChangeListenerTest {
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

    static class RecordingListener implements NamespaceChangeListener {
        String last;
        NamingEvent event;
        public void objectAdded(NamingEvent evt) { last = "objectAdded"; event = evt; }
        public void objectRemoved(NamingEvent evt) { last = "objectRemoved"; event = evt; }
        public void objectRenamed(NamingEvent evt) { last = "objectRenamed"; event = evt; }
        public void namingExceptionThrown(NamingExceptionEvent evt) { last = "namingExceptionThrown"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.event.NamespaceChangeListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean ext = false;
        for (Class<?> itf : c.getInterfaces())
            if (itf.getName().equals("javax.naming.event.NamingListener")) ext = true;
        check(ext, "extends NamingListener");

        check(hasMethod(c, "objectAdded", NamingEvent.class), "objectAdded(NamingEvent)");
        check(hasMethod(c, "objectRemoved", NamingEvent.class), "objectRemoved(NamingEvent)");
        check(hasMethod(c, "objectRenamed", NamingEvent.class), "objectRenamed(NamingEvent)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        EventContext src = eventContext();
        RecordingListener l = new RecordingListener();

        NamingEvent add = new NamingEvent(src, NamingEvent.OBJECT_ADDED,
                new Binding("n", "obj"), null, null);
        add.dispatch(l);
        check("objectAdded".equals(l.last), "OBJECT_ADDED -> objectAdded");
        check(l.event == add, "objectAdded receives same event");

        NamingEvent rem = new NamingEvent(src, NamingEvent.OBJECT_REMOVED,
                null, new Binding("o", "old"), null);
        rem.dispatch(l);
        check("objectRemoved".equals(l.last), "OBJECT_REMOVED -> objectRemoved");
        check(l.event == rem, "objectRemoved receives same event");

        NamingEvent ren = new NamingEvent(src, NamingEvent.OBJECT_RENAMED,
                new Binding("new", "v"), new Binding("old", "v"), "info");
        ren.dispatch(l);
        check("objectRenamed".equals(l.last), "OBJECT_RENAMED -> objectRenamed");
        check(l.event == ren, "objectRenamed receives same event");

        // namingExceptionThrown inherited from NamingListener is also callable directly.
        l.namingExceptionThrown(null);
        check("namingExceptionThrown".equals(l.last), "namingExceptionThrown callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
