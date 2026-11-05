/*
 * @test
 * @summary javax.naming.directory.DirContext conformance: interface hierarchy and members.
 * @run main DirContextTest
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
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;
public class DirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.DirContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.Context");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");
        check(names.contains("close"), "has method close");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dDcSurface();
        dDcGetAttributes();
        dDcModifyIntForm();
        dDcModifyItems();
        dDcBindWithAttrs();
        dDcSearchMatching();
        dDcSearchFilter();
        System.out.println("deep: DirContext contracts OK");
    }

    /** In-memory DirContext: full Context state machine plus attribute ops. */
    public static class DeepDirCtx implements DirContext {
        public final java.util.Map<String, Object> store = new java.util.TreeMap<>();
        public final java.util.Map<String, Attributes> attrs = new java.util.TreeMap<>();
        public final java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        public final java.util.List<String> ops = new java.util.ArrayList<>();

        // ---- Context surface ----
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
        public NamingEnumeration<NameClassPair> list(String n) {
            java.util.List<NameClassPair> ps = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, Object> e : store.entrySet())
                ps.add(new NameClassPair(e.getKey(),
                        e.getValue() == null ? null : e.getValue().getClass().getName()));
            return enumList(ps);
        }
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
        public Context createSubcontext(String n) { store.put(n, "ctx"); return new DeepDirCtx(); }
        public Context createSubcontext(Name n) { return createSubcontext(n.toString()); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public Object lookupLink(Name n) throws NamingException { return lookupLink(n.toString()); }
        public String getNameInNamespace() { return "deepdir://root"; }
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

        // ---- DirContext surface ----
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
            ops.add("mod[" + modOp + "]:" + n);
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
                } else {
                    throw new OperationNotSupportedException("op " + modOp);
                }
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
            ops.add("items:" + n);
        }
        public void modifyAttributes(String n, ModificationItem[] mods) throws NamingException {
            modifyAttributes(new CompositeName(n), mods);
        }
        public void bind(String n, Object o, Attributes a) { store.put(n, o); if (a != null) attrs.put(n, a); }
        public void bind(Name n, Object o, Attributes a) { bind(n.toString(), o, a); }
        public void rebind(String n, Object o, Attributes a) {
            store.put(n, o);
            if (a != null) attrs.put(n, a); else attrs.remove(n);
        }
        public void rebind(Name n, Object o, Attributes a) { rebind(n.toString(), o, a); }
        public DirContext createSubcontext(String n, Attributes a) {
            store.put(n, "ctx");
            if (a != null) attrs.put(n, a);
            return new DeepDirCtx();
        }
        public DirContext createSubcontext(Name n, Attributes a) { return createSubcontext(n.toString(), a); }
        public DirContext getSchema(String n) throws NamingException { throw new OperationNotSupportedException("schema"); }
        public DirContext getSchemaClassDefinition(Name n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public DirContext getSchemaClassDefinition(String n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public DirContext getSchema(Name n) throws NamingException { return getSchema(n.toString()); }
        public String getSchemaName(String n) throws NamingException { throw new OperationNotSupportedException("schemaName"); }
        public String getSchemaName(Name n) throws NamingException { return getSchemaName(n.toString()); }
        public NamingEnumeration<SearchResult> search(String n, Attributes matchingAttrs) throws javax.naming.NamingException {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (matches(attrs.get(name), matchingAttrs))
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m) throws javax.naming.NamingException { return search(n.toString(), m); }
        public NamingEnumeration<SearchResult> search(String n, Attributes m, String[] ids) throws javax.naming.NamingException {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (matches(attrs.get(name), m))
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name, ids)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m, String[] ids) throws javax.naming.NamingException { return search(n.toString(), m, ids); }
        public NamingEnumeration<SearchResult> search(String n, String filter, SearchControls c) throws javax.naming.NamingException {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            for (String name : new java.util.TreeSet<>(attrs.keySet()))
                if (filterMatches(attrs.get(name), filter))
                    rs.add(new SearchResult(name, store.get(name), getAttributes(name)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, String f, SearchControls c) throws javax.naming.NamingException { return search(n.toString(), f, c); }
        public NamingEnumeration<SearchResult> search(String n, String filter, Object[] args, SearchControls c) throws javax.naming.NamingException {
            String f = filter;
            if (args != null) for (int i = 0; i < args.length; i++) f = f.replace("{" + i + "}", String.valueOf(args[i]));
            return search(n, f, c);
        }
        public NamingEnumeration<SearchResult> search(Name n, String f, Object[] a, SearchControls c) throws javax.naming.NamingException { return search(n.toString(), f, a, c); }

        static boolean matches(Attributes entry, Attributes matching) throws javax.naming.NamingException {
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
        static boolean filterMatches(Attributes entry, String filter) {
            if ("(objectClass=*)".equals(filter)) return true;
            if (filter.startsWith("(") && filter.endsWith(")") && filter.contains("=")) {
                String body = filter.substring(1, filter.length() - 1);
                int eq = body.indexOf('=');
                String id = body.substring(0, eq);
                String val = body.substring(eq + 1);
                if (id.endsWith("*")) return entry.get(id.substring(0, id.length() - 1)) != null;
                if (val.equals("*")) return entry.get(id) != null; // presence: (attr=*)
                Attribute a = entry.get(id);
                return a != null && a.contains(val);
            }
            return false;
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

    static void dDcSurface() throws Exception {
        check(Context.class.isAssignableFrom(DirContext.class), "DirContext extends Context");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : DirContext.class.getMethods()) names.add(m.getName());
        for (String want : new String[] {"getAttributes", "modifyAttributes", "bind", "rebind",
                "createSubcontext", "getSchema", "getSchemaClassDefinition", "search"})
            check(names.contains(want), "declares " + want);
        check(!names.contains("getSchemaName"), "getSchemaName is not on DirContext");
        check(DirContext.ADD_ATTRIBUTE == 1, "ADD_ATTRIBUTE == 1");
        check(DirContext.REPLACE_ATTRIBUTE == 2, "REPLACE_ATTRIBUTE == 2");
        check(DirContext.REMOVE_ATTRIBUTE == 3, "REMOVE_ATTRIBUTE == 3");
    }

    static void dDcGetAttributes() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v1"));
        at.put(new BasicAttribute("sn", "v2"));
        c.bind("e1", "obj", at);
        check("v1".equals(c.getAttributes("e1").get("cn").get()), "full attribute set");
        check(c.getAttributes("e1").size() == 2, "both attributes present");
        check(c.getAttributes("missing").size() == 0, "no attrs -> empty set");
        Attributes sub = c.getAttributes("e1", new String[] {"cn"});
        check(sub.size() == 1 && "v1".equals(sub.get("cn").get()), "id subset filters");
        check(c.getAttributes("e1", new String[] {"nope"}).size() == 0, "unknown id -> empty");
        check(c.getAttributes("e1", null).size() == 2, "null ids -> all attributes (contract)");
    }

    static void dDcModifyIntForm() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        c.bind("e", "o", new BasicAttributes());
        c.modifyAttributes("e", DirContext.ADD_ATTRIBUTE, attrsOf("cn", "a"));
        check("a".equals(c.getAttributes("e").get("cn").get()), "ADD creates attribute");
        c.modifyAttributes("e", DirContext.ADD_ATTRIBUTE, attrsOf("cn", "b"));
        check(c.getAttributes("e").get("cn").size() == 2, "ADD merges values");
        c.modifyAttributes("e", DirContext.REPLACE_ATTRIBUTE, attrsOf("cn", "z"));
        check(c.getAttributes("e").get("cn").size() == 1 && "z".equals(c.getAttributes("e").get("cn").get()),
                "REPLACE swaps values");
        c.modifyAttributes("e", DirContext.REPLACE_ATTRIBUTE, attrsOf0("cn"));
        check(c.getAttributes("e").get("cn") == null, "REPLACE with empty attr removes attribute");
        c.modifyAttributes("e", DirContext.ADD_ATTRIBUTE, attrsOf("tel", "111"));
        c.modifyAttributes("e", DirContext.ADD_ATTRIBUTE, attrsOf("tel", "222"));
        c.modifyAttributes("e", DirContext.REMOVE_ATTRIBUTE, attrsOf("tel", "111"));
        check(c.getAttributes("e").get("tel").size() == 1, "REMOVE drops a single value");
        c.modifyAttributes("e", DirContext.REMOVE_ATTRIBUTE, attrsOf0("tel"));
        check(c.getAttributes("e").get("tel") == null, "REMOVE with empty attr removes attribute");
        check(c.ops.get(0).startsWith("mod[1]:e"), "ops recorded with op code");
    }

    static void dDcModifyItems() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        c.bind("e", "o", new BasicAttributes());
        c.modifyAttributes("e", new ModificationItem[] {
            new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("cn", "x")),
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("sn", "y")),
            new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("cn", "z")),
        });
        check(c.getAttributes("e").size() == 2, "both ids applied");
        check(c.getAttributes("e").get("cn").size() == 2, "two ADDs both applied in order");
        check("y".equals(c.getAttributes("e").get("sn").get()), "REPLACE applied");
        // sequence of items is observable: ADD then REMOVE of the same value
        c.modifyAttributes("e", new ModificationItem[] {
            new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("cn", "x")),
        });
        check(c.getAttributes("e").get("cn").size() == 1, "REMOVE item applied");
    }

    static void dDcBindWithAttrs() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("k", "v"));
        c.bind("n", "obj", at);
        check("obj".equals(c.lookup("n")), "object bound");
        check("v".equals(c.getAttributes("n").get("k").get()), "attrs bound with object");
        c.rebind("n", "obj2", null);
        check("obj2".equals(c.lookup("n")), "rebind replaces object");
        check(c.getAttributes("n").size() == 0, "rebind with null attrs starts a fresh attribute set");
        DirContext sub = c.createSubcontext("s", at);
        check(sub != null && sub instanceof DirContext, "createSubcontext returns DirContext");
        check("v".equals(c.getAttributes("s").get("k").get()), "subcontext attrs recorded");
        // schema hooks are part of the interface (my impl declines)
        boolean osn = false;
        try { c.getSchema("e"); }
        catch (OperationNotSupportedException e) { osn = true; }
        check(osn, "getSchema hook present");
        osn = false;
        try { c.getSchemaName("e"); }
        catch (OperationNotSupportedException e) { osn = true; }
        check(osn, "getSchemaName hook present");
    }

    static void dDcSearchMatching() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        c.bind("a", "1", attrsOf("cn", "alice"));
        c.bind("b", "2", attrsOf("cn", "bob"));
        c.bind("c", "3", attrsOf("sn", "carol"));
        java.util.List<String> names = searchNames(c.search("", attrsOf("cn", "alice")));
        check(names.equals(java.util.Arrays.asList("a")), "single match");
        names = searchNames(c.search("", null));
        check(names.size() == 3, "null matching attrs -> all entries");
        names = searchNames(c.search("", new BasicAttributes()));
        check(names.size() == 3, "empty matching attrs -> all entries");
        names = searchNames(c.search("", attrsOf("cn", "nobody")));
        check(names.isEmpty(), "no match -> empty enumeration");
        // returned attributes carry the entry values
        NamingEnumeration<SearchResult> en = c.search("", attrsOf("cn", "bob"));
        SearchResult r = en.next();
        check("bob".equals(r.getAttributes().get("cn").get()), "search result attrs");
        check("2".equals(r.getObject()), "search result object");
        // attrsToReturn subset
        c.bind("d", "4", twoAttrs("cn", "dan", "sn", "s"));
        NamingEnumeration<SearchResult> sub = c.search("", attrsOf("cn", "dan"), new String[] {"sn"});
        SearchResult sr = sub.next();
        check(sr.getAttributes().size() == 1 && "s".equals(sr.getAttributes().get("sn").get()),
                "attrsToReturn filters result attributes");
    }

    static void dDcSearchFilter() throws Exception {
        DeepDirCtx c = new DeepDirCtx();
        c.bind("a", "1", attrsOf("cn", "alice"));
        c.bind("b", "2", attrsOf("cn", "bob"));
        check(searchNames(c.search("", "(objectClass=*)", null)).size() == 2, "presence filter matches all");
        check(searchNames(c.search("", "(cn=bob)", null)).equals(java.util.Arrays.asList("b")),
                "equality filter");
        check(searchNames(c.search("", "(cn=zzz)", null)).isEmpty(), "filter no match");
        // filter args substitution happens before evaluation
        check(searchNames(c.search("", "(cn={0})", new Object[] {"alice"}, null))
                .equals(java.util.Arrays.asList("a")), "filter args substitution");
        // SearchControls ride along (impl may ignore scope)
        check(searchNames(c.search("", "(cn=*)", new SearchControls())).size() == 2, "substring filter");
    }

    static java.util.List<String> searchNames(NamingEnumeration<SearchResult> en) throws NamingException {
        java.util.List<String> out = new java.util.ArrayList<>();
        while (en.hasMore()) out.add(en.next().getName());
        return out;
    }

    static BasicAttributes attrsOf0(String id) {
        BasicAttributes a = new BasicAttributes();
        a.put(new BasicAttribute(id));   // attribute present, zero values
        return a;
    }

    static BasicAttributes attrsOf(String id, String val) {
        BasicAttributes a = new BasicAttributes();
        a.put(new BasicAttribute(id, val));
        return a;
    }

    static BasicAttributes twoAttrs(String id1, String v1, String id2, String v2) {
        BasicAttributes a = new BasicAttributes();
        a.put(new BasicAttribute(id1, v1));
        a.put(new BasicAttribute(id2, v2));
        return a;
    }

}
