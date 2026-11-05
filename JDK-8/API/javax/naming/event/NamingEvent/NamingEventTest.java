/*
 * @test
 * @summary javax.naming.event.NamingEvent conformance: type/bindings/constants.
 * @run main NamingEventTest
 */



import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;
import javax.naming.event.EventContext;
import javax.naming.event.NamingEvent;
public class NamingEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Object proxyFor(Class<?> itf) {
        return java.lang.reflect.Proxy.newProxyInstance(
            itf.getClassLoader(), new Class<?>[]{itf},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object p, java.lang.reflect.Method m, Object[] a) { return null; }
            });
    }
    public static void main(String[] args) throws Exception {
        javax.naming.event.EventContext src = (javax.naming.event.EventContext) proxyFor(javax.naming.event.EventContext.class);
        javax.naming.Binding nb = new javax.naming.Binding("cn=New", "v1");
        javax.naming.Binding ob = new javax.naming.Binding("cn=Old", "v0");
        javax.naming.event.NamingEvent e = new javax.naming.event.NamingEvent(src, javax.naming.event.NamingEvent.OBJECT_ADDED, nb, ob, "change");
        check(e.getType() == javax.naming.event.NamingEvent.OBJECT_ADDED, "getType");
        check(e.getNewBinding() == nb, "getNewBinding");
        check(e.getOldBinding() == ob, "getOldBinding");
        check("change".equals(e.getChangeInfo()), "getChangeInfo");
        check(e.getEventContext() == src, "getEventContext");
        check(javax.naming.event.NamingEvent.OBJECT_CHANGED == 3, "OBJECT_CHANGED");        testDeepContracts();
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
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
        public boolean targetMustExist() { return false; }
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
