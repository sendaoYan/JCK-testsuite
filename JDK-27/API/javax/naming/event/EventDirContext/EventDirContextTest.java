/*
 * @test
 * @summary javax.naming.event.EventDirContext conformance: white-box structure + black-box behavior.
 * @run main EventDirContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.naming.event.EventDirContext;
import javax.naming.event.NamingListener;
import javax.naming.directory.SearchControls;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.event.EventContext;
import javax.naming.event.NamingExceptionEvent;
import java.util.Hashtable;
import javax.naming.Name;

public class EventDirContextTest {
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
        Class<?> c = Class.forName("javax.naming.event.EventDirContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");

        boolean extEvent = false, extDir = false;
        for (Class<?> itf : c.getInterfaces()) {
            if (itf.getName().equals("javax.naming.event.EventContext")) extEvent = true;
            if (itf.getName().equals("javax.naming.directory.DirContext")) extDir = true;
        }
        check(extEvent, "extends EventContext");
        check(extDir, "extends DirContext");

        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, String.class,
                SearchControls.class, NamingListener.class),
                "addNamingListener(Name,String,SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, String.class,
                SearchControls.class, NamingListener.class),
                "addNamingListener(String,String,SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", javax.naming.Name.class, String.class,
                Object[].class, SearchControls.class, NamingListener.class),
                "addNamingListener(Name,String,Object[],SearchControls,NamingListener)");
        check(hasMethod(c, "addNamingListener", String.class, String.class,
                Object[].class, SearchControls.class, NamingListener.class),
                "addNamingListener(String,String,Object[],SearchControls,NamingListener)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Scope constants are inherited from EventContext (compile-time constants).
        check(EventDirContext.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(EventDirContext.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");

        // Proxy proves the search-scoped listener-registration overloads are callable.
        final int[] addCalls = new int[1];
        EventDirContext edc = (EventDirContext) Proxy.newProxyInstance(
                EventDirContext.class.getClassLoader(),
                new Class<?>[]{EventDirContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "addNamingListener": addCalls[0]++; return null;
                        case "targetMustExist": return false;
                        case "toString": return "TestEventDirContext";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return null;
                    }
                });

        NamingListener listener = evt -> { };
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        edc.addNamingListener("cn=test", "(objectClass=*)", ctls, listener);
        edc.addNamingListener(new javax.naming.CompositeName("cn=test"), "(uid=*)",
                new Object[]{"java.lang.String"}, ctls, listener);
        check(addCalls[0] == 2, "both search-scoped addNamingListener overloads invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dEdcSurface();
        dEdcFilterRegistration();
        System.out.println("deep: EventDirContext contracts OK");
    }

    static void dEdcSurface() throws Exception {
        check(DirContext.class.isAssignableFrom(EventDirContext.class),
                "EventDirContext extends DirContext");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : EventDirContext.class.getMethods()) names.add(m.getName());
        check(names.contains("addNamingListener"), "declares addNamingListener");
        check(names.contains("removeNamingListener"), "declares removeNamingListener");
        // exactly four filter-based overloads on the declared surface
        int filterForms = 0;
        for (java.lang.reflect.Method m : EventDirContext.class.getDeclaredMethods())
            if (m.getName().equals("addNamingListener")) filterForms++;
        check(filterForms == 4, "4 filter-based addNamingListener overloads (declared)");
        check(EventContext.class.isAssignableFrom(EventDirContext.class),
                "EventDirContext is an EventContext");
    }

    static class DeepEventDirCtx implements EventDirContext {
        final java.util.List<String> log = new java.util.ArrayList<>();
        // EventDirContext additions
        public void addNamingListener(Name target, String filter, SearchControls ctls, NamingListener l) {
            log.add("f:" + target + ":" + filter);
        }
        public void addNamingListener(String target, String filter, SearchControls ctls, NamingListener l) {
            log.add("f:" + target + ":" + filter);
        }
        public void addNamingListener(Name target, String filter, Object[] args, SearchControls ctls, NamingListener l) {
            log.add("fa:" + target + ":" + filter + ":" + args.length);
        }
        public void addNamingListener(String target, String filter, Object[] args, SearchControls ctls, NamingListener l) {
            log.add("fa:" + target + ":" + filter + ":" + args.length);
        }
        // EventContext surface
        public void addNamingListener(Name t, int s, NamingListener l) { log.add("n:" + t + ":" + s); }
        public void addNamingListener(String t, int s, NamingListener l) { log.add("n:" + t + ":" + s); }
        public void removeNamingListener(NamingListener l) { log.add("rm"); }
        public boolean targetMustExist() { return false; }
        public void targetRemovedFromScope(NamingExceptionEvent e, NamingListener l) { }
        // Context + DirContext surface: minimal stubs
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
        public Attributes getAttributes(Name n) { return null; }
        public Attributes getAttributes(String n) { return null; }
        public Attributes getAttributes(Name n, String[] ids) { return null; }
        public Attributes getAttributes(String n, String[] ids) { return null; }
        public void modifyAttributes(Name n, int modOp, Attributes mods) { }
        public void modifyAttributes(String n, int modOp, Attributes mods) { }
        public void modifyAttributes(Name n, ModificationItem[] mods) { }
        public void modifyAttributes(String n, ModificationItem[] mods) { }
        public void bind(Name n, Object o, Attributes a) { }
        public void bind(String n, Object o, Attributes a) { }
        public void rebind(Name n, Object o, Attributes a) { }
        public void rebind(String n, Object o, Attributes a) { }
        public DirContext createSubcontext(Name n, Attributes a) { return null; }
        public DirContext createSubcontext(String n, Attributes a) { return null; }
        public DirContext getSchemaClassDefinition(Name n) { return null; }
        public DirContext getSchemaClassDefinition(String n) { return null; }
        public DirContext getSchema(Name n) { return null; }
        public DirContext getSchema(String n) { return null; }
        public String getSchemaName(Name n) { return null; }
        public String getSchemaName(String n) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m) { return null; }
        public NamingEnumeration<SearchResult> search(String n, Attributes m) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m, String[] ids) { return null; }
        public NamingEnumeration<SearchResult> search(String n, Attributes m, String[] ids) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, String f, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(String n, String f, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, String f, Object[] a, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(String n, String f, Object[] a, SearchControls c) { return null; }
    }

    static void dEdcFilterRegistration() throws Exception {
        DeepEventDirCtx ctx = new DeepEventDirCtx();
        NamingListener sink = new DeepSink();
        ctx.addNamingListener(new CompositeName("a"), "(cn=x)", null, sink);
        ctx.addNamingListener("b", "(cn=y)", new SearchControls(), sink);
        ctx.addNamingListener(new CompositeName("c"), "(cn={0})", new Object[] {"z"}, null, sink);
        ctx.addNamingListener("d", "(cn={0}{1})", new Object[] {"p", "q"}, new SearchControls(), sink);
        check(ctx.log.get(0).equals("f:a:(cn=x)"), "Name filter registration");
        check(ctx.log.get(1).equals("f:b:(cn=y)"), "String filter registration");
        check(ctx.log.get(2).equals("fa:c:(cn={0}):1"), "filter args registration (Name)");
        check(ctx.log.get(3).equals("fa:d:(cn={0}{1}):2"), "filter args registration (String)");
        ctx.addNamingListener("e", EventContext.ONELEVEL_SCOPE, sink);
        check(ctx.log.get(4).equals("n:e:1"), "scope-based registration still available");
        ctx.removeNamingListener(sink);
        check(ctx.log.get(5).equals("rm"), "removal shared with EventContext");
    }

    static class DeepSink implements NamingListener {
        public void namingExceptionThrown(NamingExceptionEvent e) { }
    }

}
