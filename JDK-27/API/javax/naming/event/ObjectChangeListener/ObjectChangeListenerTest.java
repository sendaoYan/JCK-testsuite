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
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.NamingListener;
import java.util.Hashtable;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dOclSurface();
        dOclDispatch();
        System.out.println("deep: ObjectChangeListener contracts OK");
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

    static class DeepOcl implements ObjectChangeListener {
        final java.util.List<String> log = new java.util.ArrayList<>();
        public void objectChanged(NamingEvent e) {
            log.add(e.getNewBinding().getName() + "->" + e.getOldBinding().getName()
                    + ":" + e.getChangeInfo());
        }
        public void namingExceptionThrown(NamingExceptionEvent e) { log.add("!"); }
    }

    static void dOclSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ObjectChangeListener.class.getMethods())
            names.add(m.getName());
        check(names.contains("objectChanged"), "declares objectChanged");
        check(names.contains("namingExceptionThrown"), "inherits namingExceptionThrown");
        check(!names.contains("objectAdded"), "namespace methods are not on this interface");
        check(NamingListener.class.isAssignableFrom(ObjectChangeListener.class),
                "extends NamingListener");
    }

    static void dOclDispatch() {
        DeepCtx src = new DeepCtx();
        DeepOcl l = new DeepOcl();
        new NamingEvent(src, NamingEvent.OBJECT_CHANGED,
                new Binding("after", "v2"), new Binding("before", "v1"), "seq-7").dispatch(l);
        check(l.log.get(0).equals("after->before:seq-7"),
                "changed sees old binding, new binding and change info");
        // namespace events are NOT routed to this listener
        boolean cce = false;
        try { new NamingEvent(src, NamingEvent.OBJECT_ADDED, null, null, null).dispatch(l); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "added event does not fit object listener");
        // but the exception callback is shared
        new NamingExceptionEvent(src, new javax.naming.NamingException()).dispatch(l);
        check(l.log.get(1).equals("!"), "exception callback inherited");
    }

}
