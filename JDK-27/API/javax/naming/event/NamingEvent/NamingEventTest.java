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
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import java.util.Hashtable;
import javax.naming.event.NamingListener;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNeConstants();
        dNeConstruction();
        dNeDispatchTable();
        dNeWrongListener();
        dNeUnknownType();
        dNeNullSource();
        System.out.println("deep: NamingEvent contracts OK");
    }

    static class DeepCtx implements EventContext {
        public Object lookup(String n) { return null; }
        public Object lookup(Name n) { return null; }
        public void bind(String n, Object o) { }
        public void bind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void unbind(String n) { }
        public void unbind(Name n) { }
        public void rename(String o, String n) { }
        public void rename(Name o, Name n) { }
        public NamingEnumeration<NameClassPair> list(String n) { return null; }
        public NamingEnumeration<NameClassPair> list(Name n) { return null; }
        public NamingEnumeration<Binding> listBindings(String n) { return null; }
        public NamingEnumeration<Binding> listBindings(Name n) { return null; }
        public void destroySubcontext(String n) { }
        public void destroySubcontext(Name n) { }
        public Context createSubcontext(String n) { return null; }
        public Context createSubcontext(Name n) { return null; }
        public Object lookupLink(String n) { return null; }
        public Object lookupLink(Name n) { return null; }
        public String getNameInNamespace() { return ""; }
        public NameParser getNameParser(String n) { return null; }
        public NameParser getNameParser(Name n) { return null; }
        public String composeName(String n, String p) { return null; }
        public Name composeName(Name n, Name p) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public Hashtable<?, ?> getEnvironment() { return new Hashtable<Object, Object>(); }
        public void close() { }
        public void addNamingListener(Name t, int s, NamingListener l) { }
        public void addNamingListener(String t, int s, NamingListener l) { }
        public void removeNamingListener(NamingListener l) { }
        public boolean targetMustExist() { return false; }
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
    }

    static class DeepNsListener implements NamespaceChangeListener {
        final java.util.List<String> calls = new java.util.ArrayList<>();
        public void objectAdded(NamingEvent e) { calls.add("added:" + e.getType()); }
        public void objectRemoved(NamingEvent e) { calls.add("removed:" + e.getType()); }
        public void objectRenamed(NamingEvent e) { calls.add("renamed:" + e.getType()); }
        public void namingExceptionThrown(NamingExceptionEvent e) { calls.add("exc"); }
    }

    static class DeepObjListener implements ObjectChangeListener {
        final java.util.List<String> calls = new java.util.ArrayList<>();
        public void objectChanged(NamingEvent e) { calls.add("changed:" + e.getType()); }
        public void namingExceptionThrown(NamingExceptionEvent e) { calls.add("exc"); }
    }

    static void dNeConstants() {
        check(NamingEvent.OBJECT_ADDED == 0, "OBJECT_ADDED == 0");
        check(NamingEvent.OBJECT_REMOVED == 1, "OBJECT_REMOVED == 1");
        check(NamingEvent.OBJECT_RENAMED == 2, "OBJECT_RENAMED == 2");
        check(NamingEvent.OBJECT_CHANGED == 3, "OBJECT_CHANGED == 3");
    }

    static void dNeConstruction() {
        DeepCtx src = new DeepCtx();
        Binding nb = new Binding("n", "new");
        Binding ob = new Binding("o", "old");
        NamingEvent e = new NamingEvent(src, NamingEvent.OBJECT_CHANGED, nb, ob, "info");
        check(e.getType() == NamingEvent.OBJECT_CHANGED, "getType");
        check(e.getEventContext() == src, "getEventContext is source");
        check(e.getSource() == src, "getSource (EventObject) identical");
        check(e.getNewBinding() == nb, "new binding identity");
        check(e.getOldBinding() == ob, "old binding identity");
        check("info".equals(e.getChangeInfo()), "change info");
        // null bindings and null change info are permitted
        NamingEvent sparse = new NamingEvent(src, NamingEvent.OBJECT_RENAMED, null, null, null);
        check(sparse.getNewBinding() == null && sparse.getOldBinding() == null
                && sparse.getChangeInfo() == null, "nulls permitted");
    }

    static void dNeDispatchTable() {
        DeepCtx src = new DeepCtx();
        DeepNsListener ns = new DeepNsListener();
        DeepObjListener obj = new DeepObjListener();
        new NamingEvent(src, NamingEvent.OBJECT_ADDED, new Binding("n", "v"), null, null).dispatch(ns);
        new NamingEvent(src, NamingEvent.OBJECT_REMOVED, null, new Binding("n", "v"), null).dispatch(ns);
        new NamingEvent(src, NamingEvent.OBJECT_RENAMED, new Binding("a", 1), new Binding("b", 2), null).dispatch(ns);
        new NamingEvent(src, NamingEvent.OBJECT_CHANGED, new Binding("n", "v2"), new Binding("n", "v1"), null).dispatch(obj);
        check(ns.calls.equals(java.util.Arrays.asList("added:0", "removed:1", "renamed:2")),
                "namespace dispatch table 0->added 1->removed 2->renamed");
        check(obj.calls.equals(java.util.Arrays.asList("changed:3")), "object dispatch 3->changed");
        // dispatch returns the listener's result through the same thread (no async)
        check(ns.calls.size() == 3, "synchronous dispatch");
    }

    static void dNeWrongListener() {
        DeepCtx src = new DeepCtx();
        // OBJECT_ADDED requires a NamespaceChangeListener: an ObjectChangeListener hits a CCE
        ObjectChangeListener only = new DeepObjListener();
        boolean cce1 = false, cce2 = false;
        try {
            new NamingEvent(src, NamingEvent.OBJECT_ADDED, null, null, null).dispatch(only);
        } catch (ClassCastException e) { cce1 = true; }
        try {
            new NamingEvent(src, NamingEvent.OBJECT_CHANGED, null, null, null).dispatch(new DeepNsListener());
        } catch (ClassCastException e) { cce2 = true; }
        check(cce1, "added -> ObjectChangeListener throws CCE");
        check(cce2, "changed -> NamespaceChangeListener throws CCE");
    }

    static void dNeUnknownType() {
        DeepCtx src = new DeepCtx();
        DeepNsListener ns = new DeepNsListener();
        // an unknown event type dispatches silently (switch has no default branch)
        new NamingEvent(src, 99, null, null, null).dispatch(ns);
        new NamingEvent(src, -1, null, null, null).dispatch(ns);
        check(ns.calls.isEmpty(), "unknown type dispatches as no-op");
    }

    static void dNeNullSource() {
        boolean iae = false;
        try { new NamingEvent(null, NamingEvent.OBJECT_ADDED, null, null, null); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "null source -> IllegalArgumentException");
    }

}
