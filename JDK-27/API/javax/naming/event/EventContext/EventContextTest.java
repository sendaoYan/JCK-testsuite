/*
 * @test
 * @summary javax.naming.event.EventContext conformance: white-box structure + black-box behavior.
 * @run main EventContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.event.EventContext;
import javax.naming.event.NamingListener;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;
import javax.naming.Name;

public class EventContextTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.event.EventContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        // extends javax.naming.Context
        boolean extendsContext = false;
        for (Class<?> itf : c.getInterfaces()) {
            if (itf.getName().equals("javax.naming.Context")) extendsContext = true;
        }
        check(extendsContext, "extends javax.naming.Context");

        // method signatures
        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, int.class, NamingListener.class),
                "addNamingListener(Name,int,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, int.class, NamingListener.class),
                "addNamingListener(String,int,NamingListener)");
        check(hasMethod(c, "removeNamingListener", NamingListener.class), "removeNamingListener(NamingListener)");
        check(hasMethod(c, "targetMustExist"), "targetMustExist()");
        check(c.getMethod("targetMustExist").getReturnType() == boolean.class, "targetMustExist returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Documented scope constant values.
        check(EventContext.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(EventContext.ONELEVEL_SCOPE == 1, "ONELEVEL_SCOPE == 1");
        check(EventContext.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");

        // A proxy implementation proves the listener-registration methods are callable.
        final boolean[] called = new boolean[2];
        EventContext ec = (EventContext) Proxy.newProxyInstance(
                EventContext.class.getClassLoader(),
                new Class<?>[]{EventContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "targetMustExist": return false;
                        case "addNamingListener": called[0] = true; return null;
                        case "removeNamingListener": called[1] = true; return null;
                        case "toString": return "TestEventContext";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return null;
                    }
                });

        check(!ec.targetMustExist(), "proxy targetMustExist returns false");
        NamingListener listener = evt -> { };
        ec.addNamingListener("cn=test", EventContext.SUBTREE_SCOPE, listener);
        ec.addNamingListener(new javax.naming.CompositeName("cn=test"),
                EventContext.ONELEVEL_SCOPE, listener);
        ec.removeNamingListener(listener);
        check(called[0], "addNamingListener invoked");
        check(called[1], "removeNamingListener invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dEcConstants();
        dEcListenerRegistry();
        System.out.println("deep: EventContext contracts OK");
    }

    static void dEcConstants() {
        check(EventContext.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(EventContext.ONELEVEL_SCOPE == 1, "ONELEVEL_SCOPE == 1");
        check(EventContext.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");
        check(Context.class.isAssignableFrom(EventContext.class), "EventContext extends Context");
    }

    /** Minimal plain Context so DeepEventCtx only adds event behavior. */
    static class DeepPlainCtx implements Context {
        final java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        public Object lookup(String n) { return "V:" + n; }
        public Object lookup(Name n) { return lookup(n.toString()); }
        public void bind(String n, Object o) { }
        public void bind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void unbind(String n) { }
        public void unbind(Name n) { }
        public void rename(String o, String n) { }
        public void rename(Name o, Name n) { }
        public NamingEnumeration<NameClassPair> list(String n) {
            return new NamingEnumeration<NameClassPair>() {
                public NameClassPair next() { throw new java.util.NoSuchElementException(); }
                public boolean hasMore() { return false; }
                public void close() { }
                public NameClassPair nextElement() { throw new java.util.NoSuchElementException(); }
                public boolean hasMoreElements() { return false; }
            };
        }
        public NamingEnumeration<NameClassPair> list(Name n) { return list(n.toString()); }
        public NamingEnumeration<Binding> listBindings(String n) {
            return new NamingEnumeration<Binding>() {
                public Binding next() { throw new java.util.NoSuchElementException(); }
                public boolean hasMore() { return false; }
                public void close() { }
                public Binding nextElement() { throw new java.util.NoSuchElementException(); }
                public boolean hasMoreElements() { return false; }
            };
        }
        public NamingEnumeration<Binding> listBindings(Name n) { return listBindings(n.toString()); }
        public void destroySubcontext(String n) { }
        public void destroySubcontext(Name n) { }
        public Context createSubcontext(String n) { return new DeepPlainCtx(); }
        public Context createSubcontext(Name n) { return createSubcontext(n.toString()); }
        public Object lookupLink(String n) { return lookup(n); }
        public Object lookupLink(Name n) { return lookupLink(n.toString()); }
        public String getNameInNamespace() { return ""; }
        public NameParser getNameParser(String n) {
            return new NameParser() { public Name parse(String s) throws NamingException { return new CompositeName(s); } };
        }
        public NameParser getNameParser(Name n) { return getNameParser(n.toString()); }
        public String composeName(String n, String p) { return p + "/" + n; }
        public Name composeName(Name n, Name p) throws NamingException {
            Name r = (Name) p.clone(); r.addAll(n); return r;
        }
        public Object addToEnvironment(String k, Object v) { return env.put(k, v); }
        public Object removeFromEnvironment(String k) { return env.remove(k); }
        public Hashtable<?, ?> getEnvironment() { return (Hashtable<?, ?>) env.clone(); }
        public void close() { }
    }

    /** Recording EventContext: tracks registrations, plays back events. */
    static class DeepEventCtx extends DeepPlainCtx implements EventContext {
        final java.util.List<String> log = new java.util.ArrayList<>();
        final java.util.List<NamingListener> listeners = new java.util.ArrayList<>();
        public void addNamingListener(Name target, int scope, NamingListener l) {
            log.add("add:" + target + ":" + scope);
            listeners.add(l);
        }
        public void addNamingListener(String target, int scope, NamingListener l) {
            log.add("add:" + target + ":" + scope);
            listeners.add(l);
        }
        public void removeNamingListener(NamingListener l) {
            log.add("remove");
            while (listeners.remove(l)) { }
        }
        public boolean targetMustExist() { return false; }
        public void targetRemovedFromScope(NamingExceptionEvent evt, NamingListener l) {
            log.add("out-of-scope");
            l.namingExceptionThrown(evt);
        }
    }

    static class DeepRecorder implements NamespaceChangeListener, ObjectChangeListener {
        final java.util.List<String> events = new java.util.ArrayList<>();
        public void objectAdded(NamingEvent e) { events.add("+" + e.getType()); }
        public void objectRemoved(NamingEvent e) { events.add("-" + e.getType()); }
        public void objectRenamed(NamingEvent e) { events.add("~" + e.getType()); }
        public void objectChanged(NamingEvent e) { events.add("*" + e.getType()); }
        public void namingExceptionThrown(NamingExceptionEvent e) { events.add("!"); }
    }

    static void dEcListenerRegistry() throws Exception {
        DeepEventCtx ctx = new DeepEventCtx();
        DeepRecorder l = new DeepRecorder();
        ctx.addNamingListener(new CompositeName("a/b"), EventContext.OBJECT_SCOPE, l);
        ctx.addNamingListener("c", EventContext.SUBTREE_SCOPE, l);
        check(ctx.log.get(0).equals("add:a/b:0"), "Name registration logs target and scope");
        check(ctx.log.get(1).equals("add:c:2"), "String registration logs target and scope");
        check(ctx.listeners.size() == 2 && ctx.listeners.get(0) == ctx.listeners.get(1),
                "two registrations hold the same listener twice");
        // firing events through the recorded listener exercises the wiring
        ctx.listeners.get(0).namingExceptionThrown(new NamingExceptionEvent(ctx,
                new javax.naming.CommunicationException("down")));
        check(l.events.equals(java.util.Arrays.asList("!")), "exception callback delivered");
        ctx.removeNamingListener(l);
        check(ctx.listeners.isEmpty(), "listener removed");
        // out-of-scope callback routes to the exception method
        ctx.targetRemovedFromScope(new NamingExceptionEvent(ctx,
                new javax.naming.ServiceUnavailableException("gone")), l);
        check(l.events.equals(java.util.Arrays.asList("!", "!")), "out-of-scope -> exceptionThrown");
    }

}
