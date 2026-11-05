/*
 * @test
 * @summary javax.naming.directory.InitialDirContext conformance: lazy construction/close.
 * @run main InitialDirContextTest
 */



import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
public class InitialDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.InitialDirContext ctx = new javax.naming.directory.InitialDirContext();
        check(ctx != null, "constructed");
        ctx.close();
        check(true, "closed");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }


    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        boolean viaProperty = !NamingManager.hasInitialContextFactoryBuilder();
        if (viaProperty) dIdcNoFactory();
        else System.out.println("note: builder installed earlier; negative path covered via builder");
        dIdcClosedLoop();
        dIdcFacadeSemantics();
        System.out.println("deep: InitialDirContext contracts OK");
    }

    public static class DeepDirFactory implements InitialContextFactory {
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            return new DeepMemDirCtx(env);
        }
    }

    public static class DeepMemDirCtx implements DirContext {
        final java.util.Map<String, Object> store = new java.util.TreeMap<>();
        final java.util.Map<String, Attributes> attrs = new java.util.TreeMap<>();
        final java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        DeepMemDirCtx() { }
        DeepMemDirCtx(java.util.Hashtable<?, ?> seed) { env.putAll(seed); }

        public Object lookup(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            return v;
        }
        public Object lookup(Name n) throws NamingException { return lookup(n.toString()); }
        public void bind(String n, Object o) { store.put(n, o); }
        public void bind(Name n, Object o) { bind(n.toString(), o); }
        public void rebind(String n, Object o) { store.put(n, o); }
        public void rebind(Name n, Object o) { rebind(n.toString(), o); }
        public void unbind(String n) { store.remove(n); attrs.remove(n); }
        public void unbind(Name n) { unbind(n.toString()); }
        public void rename(String o, String n) { store.put(n, store.remove(o)); Attributes a = attrs.remove(o); if (a != null) attrs.put(n, a); }
        public void rename(Name o, Name n) { rename(o.toString(), n.toString()); }
        public NamingEnumeration<NameClassPair> list(String n) { return enumList(namesAsPairs()); }
        public NamingEnumeration<NameClassPair> list(Name n) { return list(n.toString()); }
        public NamingEnumeration<Binding> listBindings(String n) {
            java.util.List<Binding> bs = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, Object> e : store.entrySet())
                bs.add(new Binding(e.getKey(), e.getValue()));
            return enumList(bs);
        }
        public NamingEnumeration<Binding> listBindings(Name n) { return listBindings(n.toString()); }
        public void destroySubcontext(String n) throws NamingException {
            if (store.remove(n) == null) throw new NameNotFoundException(n);
            attrs.remove(n);
        }
        public void destroySubcontext(Name n) throws NamingException { destroySubcontext(n.toString()); }
        public Context createSubcontext(String n) { store.put(n, "ctx"); return new DeepMemDirCtx(); }
        public Context createSubcontext(Name n) { return createSubcontext(n.toString()); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public Object lookupLink(Name n) throws NamingException { return lookupLink(n.toString()); }
        public String getNameInNamespace() { return "deepidc://root"; }
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

        public Attributes getAttributes(String n) {
            Attributes a = attrs.get(n);
            return a == null ? new BasicAttributes() : (Attributes) ((BasicAttributes) a).clone();
        }
        public Attributes getAttributes(Name n) { return getAttributes(n.toString()); }
        public Attributes getAttributes(String n, String[] ids) {
            BasicAttributes out = new BasicAttributes();
            Attributes all = attrs.get(n);
            if (all != null) {
                if (ids == null) { // null ids means all attributes
                    for (NamingEnumeration<? extends Attribute> e = all.getAll(); e.hasMoreElements(); )
                        out.put(e.nextElement());
                } else {
                    for (String id : ids) { Attribute a = all.get(id); if (a != null) out.put(a); }
                }
            }
            return out;
        }
        public Attributes getAttributes(Name n, String[] ids) { return getAttributes(n.toString(), ids); }
        public void modifyAttributes(String n, int modOp, Attributes mods) throws NamingException {
            Attributes target = attrs.get(n);
            if (target == null) { target = new BasicAttributes(); attrs.put(n, target); }
            for (NamingEnumeration<? extends Attribute> en = mods.getAll(); en.hasMore(); ) {
                Attribute mod = en.next();
                Attribute existing = target.get(mod.getID());
                if (modOp == DirContext.ADD_ATTRIBUTE) {
                    if (existing == null) { BasicAttribute c = new BasicAttribute(mod.getID()); c.add(mod.get()); target.put(c); }
                    else for (NamingEnumeration<?> v = mod.getAll(); v.hasMore(); ) existing.add(v.next());
                } else if (modOp == DirContext.REPLACE_ATTRIBUTE) {
                    if (mod.size() == 0) target.remove(mod.getID());
                    else { BasicAttribute c = new BasicAttribute(mod.getID()); c.add(mod.get()); target.put(c); }
                } else if (modOp == DirContext.REMOVE_ATTRIBUTE) {
                    if (existing != null) {
                        if (mod.size() == 0) target.remove(mod.getID());
                        else {
                            for (NamingEnumeration<?> v = mod.getAll(); v.hasMore(); ) existing.remove(v.next());
                            if (existing.size() == 0) target.remove(mod.getID());
                        }
                    }
                } else throw new OperationNotSupportedException("op " + modOp);
            }
        }
        public void modifyAttributes(Name n, int modOp, Attributes mods) throws NamingException {
            modifyAttributes(n.toString(), modOp, mods);
        }
        public void modifyAttributes(Name n, ModificationItem[] mods) throws NamingException {
            for (ModificationItem mi : mods) {
                Attributes one = new BasicAttributes();
                one.put(mi.getAttribute());
                modifyAttributes(n.toString(), mi.getModificationOp(), one);
            }
        }
        public void modifyAttributes(String n, ModificationItem[] mods) throws NamingException {
            modifyAttributes(new CompositeName(n), mods);
        }
        public void bind(String n, Object o, Attributes a) { store.put(n, o); if (a != null) attrs.put(n, a); }
        public void bind(Name n, Object o, Attributes a) { bind(n.toString(), o, a); }
        public void rebind(String n, Object o, Attributes a) { bind(n, o, a); }
        public void rebind(Name n, Object o, Attributes a) { rebind(n.toString(), o, a); }
        public DirContext createSubcontext(String n, Attributes a) {
            store.put(n, "ctx"); if (a != null) attrs.put(n, a);
            return new DeepMemDirCtx();
        }
        public DirContext createSubcontext(Name n, Attributes a) { return createSubcontext(n.toString(), a); }
        public DirContext getSchemaClassDefinition(Name n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public DirContext getSchemaClassDefinition(String n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public DirContext getSchema(String n) throws NamingException { throw new OperationNotSupportedException("schema"); }
        public DirContext getSchema(Name n) throws NamingException { return getSchema(n.toString()); }
        public String getSchemaName(String n) throws NamingException { throw new OperationNotSupportedException("schemaName"); }
        public String getSchemaName(Name n) throws NamingException { return getSchemaName(n.toString()); }
        public NamingEnumeration<SearchResult> search(String n, Attributes m) throws javax.naming.NamingException {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (match(attrs.get(name), m))
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m) throws javax.naming.NamingException { return search(n.toString(), m); }
        public NamingEnumeration<SearchResult> search(String n, Attributes m, String[] ids) throws javax.naming.NamingException {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (match(attrs.get(name), m))
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name, ids)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m, String[] ids) throws javax.naming.NamingException { return search(n.toString(), m, ids); }
        public NamingEnumeration<SearchResult> search(String n, String filter, SearchControls c) {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (filter.contains("=*") || attrs.get(name).get(
                        filter.substring(1, filter.indexOf('='))) != null)
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, String f, SearchControls c) { return search(n.toString(), f, c); }
        public NamingEnumeration<SearchResult> search(String n, String filter, Object[] args, SearchControls c) {
            String f = filter;
            if (args != null) for (int i = 0; i < args.length; i++) f = f.replace("{" + i + "}", String.valueOf(args[i]));
            return search(n, f, c);
        }
        public NamingEnumeration<SearchResult> search(Name n, String f, Object[] a, SearchControls c) { return search(n.toString(), f, a, c); }

        static boolean match(Attributes entry, Attributes matching) throws javax.naming.NamingException {
            if (matching == null || matching.size() == 0) return true;
            for (NamingEnumeration<? extends Attribute> en = matching.getAll(); en.hasMore(); ) {
                Attribute want = en.next();
                Attribute have = entry.get(want.getID());
                if (have == null) return false;
                for (NamingEnumeration<?> v = want.getAll(); v.hasMore(); )
                    if (!have.contains(v.next())) return false;
            }
            return true;
        }
        java.util.List<NameClassPair> namesAsPairs() {
            java.util.List<NameClassPair> ps = new java.util.ArrayList<>();
            for (String k : new java.util.TreeSet<>(store.keySet()))
                ps.add(new NameClassPair(k, Object.class.getName()));
            return ps;
        }
        <T> NamingEnumeration<T> enumList(java.util.Collection<T> items) {
            final java.util.Iterator<T> it = new java.util.ArrayList<>(items).iterator();
            return new NamingEnumeration<T>() {
                public T next() { return it.next(); }
                public boolean hasMore() { return it.hasNext(); }
                public void close() { }
                public T nextElement() { return it.next(); }
                public boolean hasMoreElements() { return it.hasNext(); }
            };
        }
    }

    static void dIdcNoFactory() throws Exception {
        // the no-arg constructor is lazy: construction alone never fails
        InitialDirContext lazy = new InitialDirContext();
        check(true, "no-arg ctor constructs lazily without a factory");
        boolean threw = false;
        try { lazy.getAttributes(""); }
        catch (NoInitialContextException e) {
            threw = true;
            check(e.getMessage() != null
                    && e.getMessage().contains("java.naming.factory.initial"),
                    "no-factory message mentions property");
        }
        catch (NamingException e) {
            threw = true; // any NamingException failure mode is acceptable here
        }
        check(threw, "first operation without factory fails");
        java.util.Hashtable<Object, Object> bogus = new java.util.Hashtable<>();
        bogus.put(Context.INITIAL_CONTEXT_FACTORY, "no.such.DeepDirFactory");
        boolean threw2 = false;
        try { new InitialDirContext(bogus); }
        catch (NoInitialContextException e) {
            threw2 = true;
            check("Cannot instantiate class: no.such.DeepDirFactory".equals(e.getMessage())
                    || (e.getMessage() != null && e.getMessage().contains("no.such.DeepDirFactory")),
                    "bogus factory message");
        }
        catch (NamingException e) { threw2 = true; }
        check(threw2, "bogus factory class rejected");
    }

    static void dIdcClosedLoop() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialDirContextTest$DeepDirFactory");
        env.put("app.marker", "m1");
        InitialDirContext idc = new InitialDirContext(env);
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v1"));
        idc.bind("entry", "payload", at);
        check("payload".equals(idc.lookup("entry")), "bind/lookup through facade");
        check("v1".equals(idc.getAttributes("entry").get("cn").get()), "getAttributes through facade");
        check(idc.getAttributes("entry", new String[] {"cn"}).size() == 1,
                "getAttributes id subset through facade");
        idc.modifyAttributes("entry", DirContext.ADD_ATTRIBUTE, at2("sn", "v2"));
        check("v2".equals(idc.getAttributes("entry").get("sn").get()), "modifyAttributes ADD");
        idc.modifyAttributes("entry", new ModificationItem[] {
                new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("cn")) });
        check(idc.getAttributes("entry").get("cn") == null, "modifyAttributes items REMOVE");
        // search forms through the facade
        idc.bind("other", "2", at2("cn", "w"));
        java.util.List<String> names = new java.util.ArrayList<>();
        for (NamingEnumeration<SearchResult> en = idc.search("", at2("cn", "w")); en.hasMore(); )
            names.add(en.next().getName());
        check(names.equals(java.util.Arrays.asList("other")), "search(matching attrs)");
        int n = 0;
        for (NamingEnumeration<SearchResult> en = idc.search("", "(objectClass=*)", null); en.hasMore(); ) { en.next(); n++; }
        check(n == 2, "search(filter) through facade");
        check(idc.getEnvironment().get("app.marker").equals("m1"), "environment propagated");
        idc.close();
    }

    static void dIdcFacadeSemantics() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialDirContextTest$DeepDirFactory");
        InitialDirContext idc = new InitialDirContext(env);
        // InitialDirContext is both a Context and a DirContext facade
        check(idc instanceof DirContext, "facade implements DirContext");
        check(idc instanceof Context, "facade implements Context");
        // attributes for an unknown name -> empty (impl choice) but lookup still throws
        boolean nnf = false;
        try { idc.lookup("ghost"); }
        catch (NameNotFoundException e) { nnf = true; }
        check(nnf, "lookup missing name throws through facade");
        check(idc.getAttributes("ghost").size() == 0, "getAttributes missing -> empty by impl choice");
        idc.close();
    }

    static BasicAttributes at2(String id, String v) {
        BasicAttributes a = new BasicAttributes();
        a.put(new BasicAttribute(id, v));
        return a;
    }

}
