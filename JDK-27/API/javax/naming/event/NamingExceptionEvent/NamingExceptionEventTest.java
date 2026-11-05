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
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;

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
        testDeepContracts();
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
        public boolean targetMustExist() { return false; }
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
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
