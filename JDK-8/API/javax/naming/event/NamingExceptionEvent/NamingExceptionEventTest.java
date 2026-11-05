/*
 * @test
 * @summary javax.naming.event.NamingExceptionEvent conformance: exception/context.
 * @run main NamingExceptionEventTest
 */



import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;
import javax.naming.event.EventContext;
public class NamingExceptionEventTest {
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
        javax.naming.NamingException ex = new javax.naming.NamingException("boom");
        javax.naming.event.NamingExceptionEvent e = new javax.naming.event.NamingExceptionEvent(src, ex);
        check(e.getException() == ex, "getException");
        check(e.getEventContext() == src, "getEventContext");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNeeConstruction();
        dNeeDispatch();
        dNeeNullSource();
        System.out.println("deep: NamingExceptionEvent contracts OK");
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

    static void dNeeConstruction() {
        DeepCtx src = new DeepCtx();
        javax.naming.CommunicationException ce = new javax.naming.CommunicationException("lost");
        NamingExceptionEvent e = new NamingExceptionEvent(src, ce);
        check(e.getException() == ce, "exception identity");
        check(e.getEventContext() == src, "event context is source");
        check(e.getSource() == src, "getSource identical");
        check(e.toString().startsWith("javax.naming.event.NamingExceptionEvent[")
                && e.toString().contains("source="), "toString is the EventObject form");
        check(!e.toString().contains("lost"), "exception text not in toString (use getException)");
        // the exception is retrievable repeatedly
        check(e.getException() == e.getException(), "stable exception reference");
        // null exception is tolerated by the constructor (documented as non-null)
        NamingExceptionEvent n = new NamingExceptionEvent(src, null);
        check(n.getException() == null, "null exception stored as-is");
    }

    static void dNeeDispatch() {
        DeepCtx src = new DeepCtx();
        final java.util.List<NamingExceptionEvent> seen = new java.util.ArrayList<>();
        NamingListener l = new NamingListener() {
            public void namingExceptionThrown(NamingExceptionEvent e) { seen.add(e); }
        };
        javax.naming.ServiceUnavailableException se =
                new javax.naming.ServiceUnavailableException("down");
        NamingExceptionEvent e = new NamingExceptionEvent(src, se);
        e.dispatch(l);
        check(seen.size() == 1 && seen.get(0) == e,
                "dispatch invokes namingExceptionThrown with this event");
        // dispatch is safe for every listener subtype (no casts involved)
        class AllKinds implements NamespaceChangeListener, ObjectChangeListener {
            int hits;
            public void objectAdded(NamingEvent ev) { }
            public void objectRemoved(NamingEvent ev) { }
            public void objectRenamed(NamingEvent ev) { }
            public void objectChanged(NamingEvent ev) { }
            public void namingExceptionThrown(NamingExceptionEvent ev) { hits++; }
        }
        AllKinds k = new AllKinds();
        e.dispatch(k);
        check(k.hits == 1, "dispatch works for combined listener types");
    }

    static void dNeeNullSource() {
        boolean iae = false;
        try { new NamingExceptionEvent(null, new javax.naming.NamingException("x")); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "null source -> IllegalArgumentException");
    }

}
