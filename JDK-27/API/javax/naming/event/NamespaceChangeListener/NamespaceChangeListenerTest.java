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
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.event.NamingListener;
import java.util.Hashtable;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNsclSurface();
        dNsclDispatch();
        System.out.println("deep: NamespaceChangeListener contracts OK");
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

    static class DeepNs implements NamespaceChangeListener {
        final java.util.List<String> log = new java.util.ArrayList<>();
        public void objectAdded(NamingEvent e) { log.add("+:" + e.getNewBinding().getName()); }
        public void objectRemoved(NamingEvent e) { log.add("-:" + e.getOldBinding().getName()); }
        public void objectRenamed(NamingEvent e) { log.add("~"); }
        public void namingExceptionThrown(NamingExceptionEvent e) { log.add("!"); }
    }

    static void dNsclSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : NamespaceChangeListener.class.getMethods())
            names.add(m.getName());
        check(names.contains("objectAdded"), "declares objectAdded");
        check(names.contains("objectRemoved"), "declares objectRemoved");
        check(names.contains("objectRenamed"), "declares objectRenamed");
        check(names.contains("namingExceptionThrown"), "inherits namingExceptionThrown");
        check(NamingListener.class.isAssignableFrom(NamespaceChangeListener.class),
                "extends NamingListener");
    }

    static void dNsclDispatch() {
        DeepCtx src = new DeepCtx();
        DeepNs l = new DeepNs();
        new NamingEvent(src, NamingEvent.OBJECT_ADDED,
                new Binding("added", "v"), null, null).dispatch(l);
        new NamingEvent(src, NamingEvent.OBJECT_REMOVED,
                null, new Binding("removed", "v"), null).dispatch(l);
        new NamingEvent(src, NamingEvent.OBJECT_RENAMED,
                new Binding("new", 1), new Binding("old", 2), null).dispatch(l);
        check(l.log.get(0).equals("+:added"), "added sees new binding");
        check(l.log.get(1).equals("-:removed"), "removed sees old binding");
        check(l.log.get(2).equals("~"), "renamed sees both bindings");
        // OBJECT_CHANGED is NOT routed here (wrong listener category)
        boolean cce = false;
        try { new NamingEvent(src, NamingEvent.OBJECT_CHANGED, null, null, null).dispatch(l); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "changed event does not fit namespace listener");
    }

}
