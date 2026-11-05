/*
 * @test
 * @summary javax.naming.event.ObjectChangeListener conformance: interface hierarchy and members.
 * @run main ObjectChangeListenerTest
 */



import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import java.util.Hashtable;
public class ObjectChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.event.ObjectChangeListener");
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
        check(names.contains("objectChanged"), "has method objectChanged");        testDeepContracts();
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
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
        public boolean targetMustExist() { return false; }
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
