/*
 * @test
 * @summary javax.naming.event.NamingEvent conformance: white-box structure + black-box behavior.
 * @run main NamingEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.Binding;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;

public class NamingEventTest {
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

    // Listener implementing both namespace and object change notifications.
    static class Recorder implements NamespaceChangeListener, ObjectChangeListener {
        String last;
        NamingEvent event;
        public void objectAdded(NamingEvent e) { last = "objectAdded"; event = e; }
        public void objectRemoved(NamingEvent e) { last = "objectRemoved"; event = e; }
        public void objectRenamed(NamingEvent e) { last = "objectRenamed"; event = e; }
        public void objectChanged(NamingEvent e) { last = "objectChanged"; event = e; }
        public void namingExceptionThrown(NamingExceptionEvent e) { last = "namingExceptionThrown"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.event.NamingEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends java.util.EventObject");
        check(hasMethod(c, "getType"), "getType");
        check(hasMethod(c, "getEventContext"), "getEventContext");
        check(hasMethod(c, "getOldBinding"), "getOldBinding");
        check(hasMethod(c, "getNewBinding"), "getNewBinding");
        check(hasMethod(c, "getChangeInfo"), "getChangeInfo");
        check(hasMethod(c, "dispatch", javax.naming.event.NamingListener.class), "dispatch(NamingListener)");
        check(c.getConstructor(EventContext.class, int.class, Binding.class, Binding.class, Object.class) != null,
                "public constructor (EventContext,int,Binding,Binding,Object)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Documented event type constants.
        check(NamingEvent.OBJECT_ADDED == 0, "OBJECT_ADDED == 0");
        check(NamingEvent.OBJECT_REMOVED == 1, "OBJECT_REMOVED == 1");
        check(NamingEvent.OBJECT_RENAMED == 2, "OBJECT_RENAMED == 2");
        check(NamingEvent.OBJECT_CHANGED == 3, "OBJECT_CHANGED == 3");

        EventContext src = eventContext();
        Binding oldBd = new Binding("old/name", "old-value");
        Binding newBd = new Binding("new/name", "new-value");
        Object info = new int[]{7};

        NamingEvent evt = new NamingEvent(src, NamingEvent.OBJECT_RENAMED, newBd, oldBd, info);
        check(evt.getType() == NamingEvent.OBJECT_RENAMED, "getType");
        check(evt.getEventContext() == src, "getEventContext returns source");
        check(evt.getSource() == src, "getSource returns source");
        check(evt.getOldBinding() == oldBd, "getOldBinding identity");
        check(evt.getNewBinding() == newBd, "getNewBinding identity");
        check(evt.getChangeInfo() == info, "getChangeInfo identity");
        check("old/name".equals(evt.getOldBinding().getName()), "old binding name");
        check("new-value".equals(evt.getNewBinding().getObject()), "new binding object");

        // dispatch routes each event type to the correct listener method.
        Recorder r = new Recorder();
        NamingEvent added = new NamingEvent(src, NamingEvent.OBJECT_ADDED, newBd, null, null);
        added.dispatch(r);
        check("objectAdded".equals(r.last) && r.event == added, "OBJECT_ADDED dispatch");

        NamingEvent removed = new NamingEvent(src, NamingEvent.OBJECT_REMOVED, null, oldBd, null);
        removed.dispatch(r);
        check("objectRemoved".equals(r.last) && r.event == removed, "OBJECT_REMOVED dispatch");

        NamingEvent renamed = new NamingEvent(src, NamingEvent.OBJECT_RENAMED, newBd, oldBd, null);
        renamed.dispatch(r);
        check("objectRenamed".equals(r.last) && r.event == renamed, "OBJECT_RENAMED dispatch");

        NamingEvent changed = new NamingEvent(src, NamingEvent.OBJECT_CHANGED, newBd, oldBd, "change");
        changed.dispatch(r);
        check("objectChanged".equals(r.last) && r.event == changed, "OBJECT_CHANGED dispatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
