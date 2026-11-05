/*
 * @test
 * @summary javax.naming.ldap.InitialLdapContext conformance: lazy construction/close.
 * @run main InitialLdapContextTest
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
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
public class InitialLdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.InitialLdapContext ctx = new javax.naming.ldap.InitialLdapContext();
        check(ctx != null, "constructed");
        ctx.close();
        check(true, "closed");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        boolean viaProperty = !NamingManager.hasInitialContextFactoryBuilder();
        if (viaProperty) dIlcClosedLoop();
        else System.out.println("note: builder installed earlier; property path noted");
        dIlcWrongContextTypes();
        System.out.println("deep: InitialLdapContext contracts OK");
    }

    public static class DeepLdapFactory implements InitialContextFactory {
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            return new DeepRecLdapCtx(env);
        }
    }

    /** Recording LdapContext: reads env like a provider would. */
    public static class DeepRecLdapCtx implements LdapContext {
        final java.util.Hashtable<Object, Object> env;
        final java.util.List<String> log = new java.util.ArrayList<>();
        Control[] connCtls, reqCtls;
        int reconnectCalls;
        DeepRecLdapCtx(java.util.Hashtable<?, ?> e) {
            env = new java.util.Hashtable<>();
            if (e != null) env.putAll((java.util.Hashtable<?, ?>) e);
            Object bind = env.get("java.naming.ldap.control.connect");
            if (bind instanceof Control[]) connCtls = (Control[]) bind;
        }
        public ExtendedResponse extendedOperation(ExtendedRequest req) {
            log.add("ext:" + req.getID());
            return new DeepExtResp(req.getID(), req.getEncodedValue());
        }
        public LdapContext newInstance(Control[] c) { return new DeepRecLdapCtx(env); }
        public void reconnect(Control[] c) { reconnectCalls++; connCtls = c; }
        public Control[] getConnectControls() { return connCtls; }
        public void setRequestControls(Control[] c) { reqCtls = c; }
        public Control[] getRequestControls() { return reqCtls; }
        public Control[] getResponseControls() { return new Control[0]; }
        // Context / DirContext surface: minimal in-memory store
        final java.util.Map<String, Object> store = new java.util.TreeMap<>();
        final java.util.Map<String, Attributes> attrs = new java.util.TreeMap<>();
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
        public void unbind(String n) { store.remove(n); }
        public void unbind(Name n) { unbind(n.toString()); }
        public void rename(String o, String n) { store.put(n, store.remove(o)); }
        public void rename(Name o, Name n) { rename(o.toString(), n.toString()); }
        public NamingEnumeration<NameClassPair> list(String n) {
            java.util.List<NameClassPair> ps = new java.util.ArrayList<>();
            for (String k : store.keySet()) ps.add(new NameClassPair(k, Object.class.getName()));
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
        public void destroySubcontext(String n) { store.remove(n); }
        public void destroySubcontext(Name n) { destroySubcontext(n.toString()); }
        public Context createSubcontext(String n) { store.put(n, "ctx"); return new DeepRecLdapCtx(env); }
        public Context createSubcontext(Name n) { return createSubcontext(n.toString()); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public Object lookupLink(Name n) throws NamingException { return lookupLink(n.toString()); }
        public String getNameInNamespace() { return "deeprec://root"; }
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
        public Attributes getAttributes(Name n) { return getAttributes(n.toString()); }
        public Attributes getAttributes(String n) {
            Attributes a = attrs.get(n);
            return a == null ? new BasicAttributes() : (Attributes) ((BasicAttributes) a).clone();
        }
        public Attributes getAttributes(Name n, String[] ids) { return getAttributes(n.toString(), ids); }
        public Attributes getAttributes(String n, String[] ids) {
            BasicAttributes out = new BasicAttributes(); Attributes all = attrs.get(n);
            if (all != null) {
                if (ids == null) { // null ids means all attributes
                    for (NamingEnumeration<? extends Attribute> e = all.getAll(); e.hasMoreElements(); )
                        out.put(e.nextElement());
                } else {
                    for (String id : ids) { Attribute x = all.get(id); if (x != null) out.put(x); }
                }
            }
            return out;
        }
        public void modifyAttributes(Name n, int op, Attributes mods) { }
        public void modifyAttributes(String n, int op, Attributes mods) { }
        public void modifyAttributes(Name n, ModificationItem[] mods) { }
        public void modifyAttributes(String n, ModificationItem[] mods) { }
        public void bind(Name n, Object o, Attributes a) { bind(n.toString(), o); if (a != null) attrs.put(n.toString(), a); }
        public void bind(String n, Object o, Attributes a) { store.put(n, o); if (a != null) attrs.put(n, a); }
        public void rebind(Name n, Object o, Attributes a) { bind(n, o, a); }
        public void rebind(String n, Object o, Attributes a) { bind(n, o, a); }
        public DirContext createSubcontext(Name n, Attributes a) { store.put(n.toString(), "ctx"); return new DeepRecLdapCtx(env); }
        public DirContext createSubcontext(String n, Attributes a) { store.put(n, "ctx"); return new DeepRecLdapCtx(env); }
        public DirContext getSchema(Name n) throws NamingException { throw new OperationNotSupportedException("schema"); }
        public DirContext getSchema(String n) throws NamingException { throw new OperationNotSupportedException("schema"); }
        public String getSchemaName(Name n) throws NamingException { throw new OperationNotSupportedException("schemaName"); }
        public String getSchemaName(String n) throws NamingException { throw new OperationNotSupportedException("schemaName"); }
        public DirContext getSchemaClassDefinition(Name n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public DirContext getSchemaClassDefinition(String n) throws NamingException { throw new OperationNotSupportedException("schemaClass"); }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m) { return search(n.toString(), m); }
        public NamingEnumeration<SearchResult> search(String n, Attributes m) {
            java.util.List<SearchResult> rs = new java.util.ArrayList<>();
            rs.add(new SearchResult("hit", "o", getAttributes(n)));
            return enumList(rs);
        }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m, String[] ids) { return search(n, m); }
        public NamingEnumeration<SearchResult> search(String n, Attributes m, String[] ids) { return search(n, m); }
        public NamingEnumeration<SearchResult> search(Name n, String f, SearchControls c) { return search(n.toString(), f, c); }
        public NamingEnumeration<SearchResult> search(String n, String f, SearchControls c) { return search(n, (Attributes) null); }
        public NamingEnumeration<SearchResult> search(Name n, String f, Object[] a, SearchControls c) { return search(n, f, c); }
        public NamingEnumeration<SearchResult> search(String n, String f, Object[] a, SearchControls c) { return search(n, f, c); }
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

    public static class DeepExtResp implements ExtendedResponse {
        final String id; final byte[] val;
        DeepExtResp(String id, byte[] val) { this.id = id; this.val = val; }
        public String getID() { return id; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dIlcClosedLoop() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialLdapContextTest$DeepLdapFactory");
        Control[] connCtls = {new BasicControl("1.1", true, null)};
        InitialLdapContext ic = new InitialLdapContext(env, connCtls);
        // facade forces LDAPv3 into the environment seen by the provider
        check("3".equals(ic.getEnvironment().get("java.naming.ldap.version")),
                "java.naming.ldap.version forced to 3");
        // connection controls ride along via the bind-controls env property
        Control[] seen = ic.getConnectControls();
        check(seen != null && seen.length == 1 && "1.1".equals(seen[0].getID()),
                "connect controls delivered to provider");
        // request control lifecycle through the facade
        Control[] req = {new BasicControl("2.2")};
        ic.setRequestControls(req);
        check(ic.getRequestControls().length == 1 && "2.2".equals(ic.getRequestControls()[0].getID()),
                "request controls set/get");
        ic.setRequestControls(null);
        check(ic.getRequestControls() == null, "null clears request controls");
        // extended operation delegation
        javax.naming.ldap.ExtendedRequest ext = new javax.naming.ldap.ExtendedRequest() {
            public String getID() { return "1.2.3.4"; }
            public byte[] getEncodedValue() { return new byte[]{1}; }
            public ExtendedResponse createExtendedResponse(String id, byte[] b, int o, int l) {
                return new DeepExtResp(id, b);
            }
        };
        ExtendedResponse resp = ic.extendedOperation(ext);
        check("1.2.3.4".equals(resp.getID()), "extendedOperation through facade");
        // newInstance and reconnect delegate
        LdapContext copy = ic.newInstance(req);
        check(copy != null && copy instanceof LdapContext, "newInstance through facade");
        ic.reconnect(connCtls);
        check("1.1".equals(ic.getConnectControls()[0].getID()), "reconnect through facade");
        check(ic.getResponseControls() != null, "response controls readable");
        // regular DirContext operations still work through the facade
        ic.bind("k", "v");
        check("v".equals(ic.lookup("k")), "bind/lookup through facade");
        check(ic.getAttributes("k").size() == 0, "getAttributes through facade");
        check(ic.search("", (Attributes) null).hasMore(), "search through facade");
        ic.close();
    }

    static void dIlcWrongContextTypes() throws Exception {
        // factory returning a plain Context: DirContext ops fail first
        java.util.Hashtable<Object, Object> plain = new java.util.Hashtable<>();
        plain.put(Context.INITIAL_CONTEXT_FACTORY, "InitialLdapContextTest$DeepPlainFactory");
        InitialLdapContext ic1 = new InitialLdapContext(plain, null);
        boolean nce1 = false;
        try { ic1.getAttributes("x"); }
        catch (NotContextException e) {
            nce1 = true;
            check("Not an instance of DirContext".equals(e.getMessage()),
                    "plain Context -> DirContext check message");
        }
        check(nce1, "plain Context rejected for directory ops");
        // factory returning a plain DirContext: LDAP-specific ops fail
        java.util.Hashtable<Object, Object> dirOnly = new java.util.Hashtable<>();
        dirOnly.put(Context.INITIAL_CONTEXT_FACTORY, "InitialLdapContextTest$DeepDirOnlyFactory");
        InitialLdapContext ic2 = new InitialLdapContext(dirOnly, null);
        boolean nce2 = false;
        try { ic2.setRequestControls(null); }
        catch (NotContextException e) {
            nce2 = true;
            check("Not an instance of LdapContext".equals(e.getMessage()),
                    "DirContext -> LdapContext check message");
        }
        check(nce2, "plain DirContext rejected for LDAP ops");
        check(ic2.getAttributes("x").size() == 0, "directory ops still work on it");
    }

    public static class DeepPlainFactory implements InitialContextFactory {
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            return new DeepPlainCtx();
        }
    }

    public static class DeepPlainCtx implements Context {
        public Object lookup(String n) { return "P:" + n; }
        public Object lookup(Name n) { return lookup(n.toString()); }
        public void bind(String n, Object o) { } public void bind(Name n, Object o) { }
        public void rebind(String n, Object o) { } public void rebind(Name n, Object o) { }
        public void unbind(String n) { } public void unbind(Name n) { }
        public void rename(String o, String n) { } public void rename(Name o, Name n) { }
        public NamingEnumeration<NameClassPair> list(String n) { return null; }
        public NamingEnumeration<NameClassPair> list(Name n) { return null; }
        public NamingEnumeration<Binding> listBindings(String n) { return null; }
        public NamingEnumeration<Binding> listBindings(Name n) { return null; }
        public void destroySubcontext(String n) { } public void destroySubcontext(Name n) { }
        public Context createSubcontext(String n) { return null; } public Context createSubcontext(Name n) { return null; }
        public Object lookupLink(String n) { return null; } public Object lookupLink(Name n) { return null; }
        public String getNameInNamespace() { return ""; }
        public NameParser getNameParser(String n) { return null; } public NameParser getNameParser(Name n) { return null; }
        public String composeName(String n, String p) { return null; } public Name composeName(Name n, Name p) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public Hashtable<?, ?> getEnvironment() { return new Hashtable<Object, Object>(); }
        public void close() { }
    }

    public static class DeepDirOnlyFactory implements InitialContextFactory {
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            return new DeepDirOnlyCtx();
        }
    }

    public static class DeepDirOnlyCtx extends DeepPlainCtx implements DirContext {
        public Attributes getAttributes(Name n) { return new BasicAttributes(); }
        public Attributes getAttributes(String n) { return new BasicAttributes(); }
        public Attributes getAttributes(Name n, String[] ids) { return new BasicAttributes(); }
        public Attributes getAttributes(String n, String[] ids) { return new BasicAttributes(); }
        public void modifyAttributes(Name n, int op, Attributes m) { }
        public void modifyAttributes(String n, int op, Attributes m) { }
        public void modifyAttributes(Name n, ModificationItem[] m) { }
        public void modifyAttributes(String n, ModificationItem[] m) { }
        public void bind(Name n, Object o, Attributes a) { }
        public void bind(String n, Object o, Attributes a) { }
        public void rebind(Name n, Object o, Attributes a) { }
        public void rebind(String n, Object o, Attributes a) { }
        public DirContext createSubcontext(Name n, Attributes a) { return this; }
        public DirContext createSubcontext(String n, Attributes a) { return this; }
        public DirContext getSchema(Name n) { return null; }
        public DirContext getSchema(String n) { return null; }
        public String getSchemaName(Name n) { return null; }
        public String getSchemaName(String n) { return null; }
        public DirContext getSchemaClassDefinition(Name n) { return null; }
        public DirContext getSchemaClassDefinition(String n) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m) { return null; }
        public NamingEnumeration<SearchResult> search(String n, Attributes m) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, Attributes m, String[] ids) { return null; }
        public NamingEnumeration<SearchResult> search(String n, Attributes m, String[] ids) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, String f, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(String n, String f, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(Name n, String f, Object[] a, SearchControls c) { return null; }
        public NamingEnumeration<SearchResult> search(String n, String f, Object[] a, SearchControls c) { return null; }
    }

}
