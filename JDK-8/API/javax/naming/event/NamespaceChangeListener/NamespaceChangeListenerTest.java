/*
 * @test
 * @summary javax.naming.event.NamespaceChangeListener conformance: interface hierarchy and members.
 * @run main NamespaceChangeListenerTest
 */



import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import java.util.Hashtable;
public class NamespaceChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.event.NamespaceChangeListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.event.NamingListener");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("namingExceptionThrown"), "has method namingExceptionThrown");
        check(names.contains("objectAdded"), "has method objectAdded");
        check(names.contains("objectRemoved"), "has method objectRemoved");        testDeepContracts();
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
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
        public boolean targetMustExist() { return false; }
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
