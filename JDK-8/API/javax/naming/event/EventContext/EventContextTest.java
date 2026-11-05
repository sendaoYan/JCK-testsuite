/*
 * @test
 * @summary javax.naming.event.EventContext conformance: interface hierarchy and members.
 * @run main EventContextTest
 */



import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;
public class EventContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.event.EventContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.Context");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addNamingListener"), "has method addNamingListener");
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");        testDeepContracts();
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
