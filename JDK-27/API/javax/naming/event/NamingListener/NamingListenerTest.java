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
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNlSurface();
        dNlInvocation();
        dNlSubtypes();
        System.out.println("deep: NamingListener contracts OK");
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

    static void dNlSurface() throws Exception {
        java.lang.reflect.Method[] ms = NamingListener.class.getMethods();
        check(ms.length == 1 && ms[0].getName().equals("namingExceptionThrown"),
                "exactly one method: namingExceptionThrown");
        check(java.util.EventListener.class.isAssignableFrom(NamingListener.class),
                "extends java.util.EventListener");
        check(NamespaceChangeListener.class.isAssignableFrom(NamingListener.class) == false
                && NamingListener.class.isAssignableFrom(NamespaceChangeListener.class),
                "NamespaceChangeListener is a subtype");
        check(NamingListener.class.isAssignableFrom(ObjectChangeListener.class),
                "ObjectChangeListener is a subtype");
    }

    static void dNlInvocation() {
        final java.util.List<String> log = new java.util.ArrayList<>();
        NamingListener l = new NamingListener() {
            public void namingExceptionThrown(NamingExceptionEvent e) {
                log.add("seen:" + e.getException().getMessage());
            }
        };
        DeepCtx src = new DeepCtx();
        javax.naming.InterruptedNamingException ine =
                new javax.naming.InterruptedNamingException("interrupted");
        new NamingExceptionEvent(src, ine).dispatch(l);
        check(log.equals(java.util.Arrays.asList("seen:interrupted")),
                "listener invoked with the thrown exception");
    }

    static void dNlSubtypes() {
        // a single listener may register interest in several categories at once
        class Combined implements NamespaceChangeListener, ObjectChangeListener {
            final java.util.List<String> log = new java.util.ArrayList<>();
            public void objectAdded(NamingEvent e) { log.add("+"); }
            public void objectRemoved(NamingEvent e) { log.add("-"); }
            public void objectRenamed(NamingEvent e) { log.add("~"); }
            public void objectChanged(NamingEvent e) { log.add("*"); }
            public void namingExceptionThrown(NamingExceptionEvent e) { log.add("!"); }
        }
        Combined c = new Combined();
        check(c instanceof NamingListener, "combined listener is a NamingListener");
        DeepCtx src = new DeepCtx();
        new NamingEvent(src, NamingEvent.OBJECT_ADDED, null, null, null).dispatch(c);
        new NamingEvent(src, NamingEvent.OBJECT_CHANGED, null, null, null).dispatch(c);
        new NamingExceptionEvent(src, new javax.naming.NamingException()).dispatch(c);
        check(c.log.equals(java.util.Arrays.asList("+", "*", "!")),
                "all callbacks routed through one object");
    }

}
