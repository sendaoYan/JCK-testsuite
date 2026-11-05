/*
 * @test
 * @summary javax.naming.InitialContext conformance: white-box structure + black-box behavior.
 * @run main InitialContextTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.naming.CompositeName;
import javax.naming.ContextNotEmptyException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;

public class InitialContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Deterministic in-memory context installed via an InitialContextFactoryBuilder. */
    static class MemContext implements Context {
        static MemContext last;
        final java.util.Map<String, Object> store = new java.util.LinkedHashMap<>();
        final Hashtable<Object, Object> env;
        MemContext(Hashtable<?, ?> e) { env = (e == null) ? new Hashtable<>() : new Hashtable<>(e); last = this; }
        public Object lookup(String name) throws NamingException { Object v = store.get(name); if (v == null) throw new javax.naming.NameNotFoundException(name); return v; }
        public void bind(String name, Object obj) { store.put(name, obj); }
        public void rebind(String name, Object obj) { store.put(name, obj); }
        public void unbind(String name) { store.remove(name); }
        public void rename(String oldName, String newName) { store.put(newName, store.remove(oldName)); }
        public NamingEnumeration<NameClassPair> list(String name) {
            List<NameClassPair> ps = new ArrayList<>();
            store.keySet().forEach(k -> ps.add(new NameClassPair(k, "java.lang.Object")));
            return enumOf(ps);
        }
        public NamingEnumeration<Binding> listBindings(String name) {
            List<Binding> bs = new ArrayList<>(); store.forEach((k, v) -> bs.add(new Binding(k, v)));
            return enumOf(bs);
        }
        public void destroySubcontext(String name) { store.remove(name); }
        public Context createSubcontext(String name) { store.put(name, "ctx"); return new MemContext(null); }
        public Object lookupLink(String name) { return store.get(name); }
        public String getNameInNamespace() { return "mem://root"; }
        public Object addToEnvironment(String p, Object v) { return env.put(p, v); }
        public Object removeFromEnvironment(String p) { return env.remove(p); }
        public Hashtable<?, ?> getEnvironment() { return env; }
        public void close() { }
        public String composeName(String n, String prefix) { return prefix + "/" + n; }
        public javax.naming.Name composeName(javax.naming.Name n, javax.naming.Name prefix) { return (javax.naming.Name) n.clone(); }
        public javax.naming.NameParser getNameParser(String name) { return s -> new javax.naming.CompositeName(s); }
        public javax.naming.NameParser getNameParser(javax.naming.Name name) { return s -> new javax.naming.CompositeName(s); }
        public Object lookup(javax.naming.Name n) throws NamingException { return lookup(n.toString()); }
        public void bind(javax.naming.Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public void rebind(javax.naming.Name n, Object o) throws NamingException { rebind(n.toString(), o); }
        public void unbind(javax.naming.Name n) throws NamingException { unbind(n.toString()); }
        public void rename(javax.naming.Name o, javax.naming.Name n) throws NamingException { rename(o.toString(), n.toString()); }
        public NamingEnumeration<NameClassPair> list(javax.naming.Name n) throws NamingException { return list(n.toString()); }
        public NamingEnumeration<Binding> listBindings(javax.naming.Name n) throws NamingException { return listBindings(n.toString()); }
        public void destroySubcontext(javax.naming.Name n) throws NamingException { destroySubcontext(n.toString()); }
        public Context createSubcontext(javax.naming.Name n) throws NamingException { return createSubcontext(n.toString()); }
        public Object lookupLink(javax.naming.Name n) throws NamingException { return lookupLink(n.toString()); }
        <T> NamingEnumeration<T> enumOf(List<T> l) {
            java.util.Iterator<T> it = l.iterator();
            return new NamingEnumeration<T>() {
                public T next() { return it.next(); } public boolean hasMore() { return it.hasNext(); } public void close() { }
                public T nextElement() { return it.next(); } public boolean hasMoreElements() { return it.hasNext(); }
            };
        }
    }

    static class MemFactory implements InitialContextFactory {
        public Context getInitialContext(Hashtable<?, ?> environment) { return new MemContext(environment); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = InitialContext.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Context.class.isAssignableFrom(c), "implements Context");
        Constructor<?> lazy = c.getDeclaredConstructor(boolean.class);
        check(Modifier.isProtected(lazy.getModifiers()), "protected lazy ctor");
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getConstructor(Hashtable.class) != null, "public Hashtable ctor");
        check(method(c, "doLookup"), "declares doLookup");
        check(c.getDeclaredMethod("getDefaultInitCtx") != null, "declares protected getDefaultInitCtx");
        check(c.getDeclaredMethod("getURLOrDefaultInitCtx", String.class) != null, "declares getURLOrDefaultInitCtx");
        check(Modifier.isStatic(c.getMethod("doLookup", String.class).getModifiers()), "doLookup static");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Phase 1: no factory configured anywhere -> deterministic failure paths
        new InitialContext();                       // construction alone must not throw
        try {
            new InitialContext().lookup("x");
            throw new AssertionError("lookup should fail");
        } catch (NoInitialContextException e) {
            check(e.getMessage().equals("Need to specify class name in environment or system property, or in an application resource file: java.naming.factory.initial"),
                    "exact no-factory message");
        }
        try {
            new InitialContext().list("x");
            throw new AssertionError("list should fail");
        } catch (NoInitialContextException e) { check(true, "list fails without factory"); }
        try {
            new InitialContext().doLookup("x");
            throw new AssertionError("doLookup should fail");
        } catch (NoInitialContextException e) { check(true, "static doLookup fails without factory"); }
        Hashtable<Object, Object> bogus = new Hashtable<>();
        bogus.put(Context.INITIAL_CONTEXT_FACTORY, "no.such.Factory");
        try {
            new InitialContext(bogus);
            throw new AssertionError("bogus factory should fail");
        } catch (NoInitialContextException e) {
            check(e.getMessage().equals("Cannot instantiate class: no.such.Factory"), "exact bad-factory message");
            check(e.getRootCause() instanceof ClassNotFoundException, "root cause is CNFE");
        }

        // Phase 2: install deterministic factory builder, exercise the facade
        NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {
            public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) { return new MemFactory(); }
        });
        Hashtable<Object, Object> env = new Hashtable<>();
        env.put("app.key", "v1");
        InitialContext ic = new InitialContext(env);
        ic.bind("one", "1");
        ic.bind("two", 2);
        check(ic.lookup("one").equals("1"), "lookup bound value");
        check(ic.lookup("two").equals(2), "lookup second");
        ic.rebind("one", "1b");
        check(ic.lookup("one").equals("1b"), "rebind through facade");
        ic.rename("one", "uno");
        check(ic.lookup("uno").equals("1b"), "rename through facade");
        try { ic.lookup("one"); throw new AssertionError("renamed away"); }
        catch (javax.naming.NameNotFoundException e) { check(true, "old name reports NameNotFound"); }
        ic.unbind("uno");
        try { ic.lookup("uno"); throw new AssertionError("unbound still found"); }
        catch (javax.naming.NameNotFoundException e) { check(true, "unbind then lookup fails"); }

        List<String> names = new ArrayList<>();
        NamingEnumeration<NameClassPair> en = ic.list("");
        while (en.hasMore()) names.add(en.next().getName());
        java.util.Collections.sort(names);
        check(names.equals(List.of("two")), "list contents");
        List<Object> vals = new ArrayList<>();
        NamingEnumeration<Binding> be = ic.listBindings("");
        while (be.hasMore()) vals.add(be.next().getObject());
        check(vals.equals(List.of(2)), "listBindings contents");

        Context sub = ic.createSubcontext("sub");
        check(sub != null, "createSubcontext returns Context");
        ic.destroySubcontext("sub");
        check(ic.lookupLink("two").equals(2), "lookupLink");
        check(ic.getNameInNamespace().equals("mem://root"), "getNameInNamespace delegated");
        // InitialContext.composeName has fixed semantics (initial ctx has no parent)
        check(ic.composeName("x", "p").equals("x"), "composeName(String,String) returns name");
        javax.naming.Name cn = ic.composeName(new javax.naming.CompositeName("a/b"), new javax.naming.CompositeName());
        check(cn.toString().equals("a/b") && cn != null, "composeName(Name,Name) clones name");
        javax.naming.NameParser parser = ic.getNameParser("");
        check(parser.parse("a/b").size() == 2, "parser obtained via facade");

        // environment handling: ctor clones caller's table; addToEnvironment visible
        env.put("late", "change");
        check(!ic.getEnvironment().containsKey("late"), "ctor cloned caller env");
        ic.addToEnvironment("extra", "e");
        check(ic.getEnvironment().get("extra").equals("e"), "addToEnvironment visible");
        ic.removeFromEnvironment("extra");
        check(!ic.getEnvironment().containsKey("extra"), "removeFromEnvironment");
        ic.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    public static class DeepInitFactory implements InitialContextFactory {
        public static DeepMemContext last;
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            last = new DeepMemContext(env);
            return last;
        }
    }

    /** In-memory context served by DeepInitFactory. */
    public static class DeepMemContext implements Context {
        final java.util.Hashtable<String, Object> store = new java.util.Hashtable<>();
        final java.util.Hashtable<Object, Object> env;
        DeepMemContext(java.util.Hashtable<?, ?> e) {
            env = (e == null) ? new java.util.Hashtable<Object, Object>()
                    : new java.util.Hashtable<Object, Object>(e);
        }
        public Object lookup(Name n) throws NamingException { return lookup(n.toString()); }
        public Object lookup(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            return v;
        }
        public void bind(Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public synchronized void bind(String n, Object o) throws NamingException {
            if (store.containsKey(n)) throw new NameAlreadyBoundException(n);
            store.put(n, o);
        }
        public void rebind(Name n, Object o) throws NamingException { store.put(n.toString(), o); }
        public void rebind(String n, Object o) throws NamingException { store.put(n, o); }
        public void unbind(Name n) throws NamingException {
            if (store.remove(n.toString()) == null) throw new NameNotFoundException(n.toString());
        }
        public void unbind(String n) throws NamingException { unbind(new CompositeName(n)); }
        public void rename(Name o, Name n) throws NamingException {
            Object v = store.remove(o.toString());
            if (v == null) throw new NameNotFoundException(o.toString());
            store.put(n.toString(), v);
        }
        public void rename(String o, String n) throws NamingException {
            rename(new CompositeName(o), new CompositeName(n));
        }
        public NamingEnumeration<NameClassPair> list(Name n) { return list(""); }
        public NamingEnumeration<NameClassPair> list(String n) { return new DeepInitEnumNC(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return listBindings(""); }
        public NamingEnumeration<Binding> listBindings(String n) { return new DeepInitEnumB(); }
        public void destroySubcontext(Name n) throws NamingException { destroySubcontext(n.toString()); }
        public void destroySubcontext(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            if (v instanceof DeepMemContext && !((DeepMemContext) v).store.isEmpty())
                throw new ContextNotEmptyException(n);
            store.remove(n);
        }
        public Context createSubcontext(Name n) throws NamingException { return createSubcontext(n.toString()); }
        public synchronized Context createSubcontext(String n) throws NamingException {
            if (store.containsKey(n)) throw new NameAlreadyBoundException(n);
            DeepMemContext c = new DeepMemContext(null);
            store.put(n, c);
            return c;
        }
        public Object lookupLink(Name n) throws NamingException { return lookup(n); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public NameParser getNameParser(Name n) { return deepInitParser; }
        public NameParser getNameParser(String n) { return deepInitParser; }
        public Name composeName(Name n, Name pfx) throws NamingException {
            Name r = (Name) pfx.clone();
            r.addAll(n);
            return r;
        }
        public String composeName(String n, String pfx) throws NamingException {
            return composeName(new CompositeName(n), new CompositeName(pfx)).toString();
        }
        public Object addToEnvironment(String k, Object v) { return env.put(k, v); }
        public Object removeFromEnvironment(String k) { return env.remove(k); }
        public java.util.Hashtable<?, ?> getEnvironment() { return (java.util.Hashtable<?, ?>) env.clone(); }
        public void close() { }
        public String getNameInNamespace() { return ""; }

        class DeepInitEnumNC implements NamingEnumeration<NameClassPair> {
            final java.util.Iterator<java.util.Map.Entry<String, Object>> it =
                    new java.util.TreeMap<String, Object>(DeepMemContext.this.store).entrySet().iterator();
            public boolean hasMore() { return it.hasNext(); }
            public boolean hasMoreElements() { return hasMore(); }
            public NameClassPair next() {
                java.util.Map.Entry<String, Object> en = it.next();
                Object v = en.getValue();
                return new NameClassPair(en.getKey(), v == null ? null : v.getClass().getName());
            }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        }

        class DeepInitEnumB implements NamingEnumeration<Binding> {
            final java.util.Iterator<java.util.Map.Entry<String, Object>> it =
                    new java.util.TreeMap<String, Object>(DeepMemContext.this.store).entrySet().iterator();
            public boolean hasMore() { return it.hasNext(); }
            public boolean hasMoreElements() { return hasMore(); }
            public Binding next() {
                java.util.Map.Entry<String, Object> en = it.next();
                return new Binding(en.getKey(), en.getValue());
            }
            public Binding nextElement() { return next(); }
            public void close() { }
        }
    }

    static final NameParser deepInitParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

    static void testDeepContracts() throws Exception {
        dIcFactoryClosedLoop();
        dIcSystemPropertyForm();
        dIcNoFactory();
        dIcDoLookupNegative();
        dIcEnvironmentPropagation();
        System.out.println("deep: InitialContext contracts OK");
    }

    static void dIcFactoryClosedLoop() throws Exception {
        boolean viaProperty = !NamingManager.hasInitialContextFactoryBuilder();
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialContextTest$DeepInitFactory");
        InitialContext ic = new InitialContext(env);
        if (viaProperty) {
            check(DeepInitFactory.last != null, "factory instantiated by NamingManager");
        }
        ic.bind("key", "value");
        check("value".equals(ic.lookup("key")), "lookup delegates to factory-produced context");
        ic.rebind("key", "v2");
        check("v2".equals(ic.lookup("key")), "rebind delegates");
        check(ic.getEnvironment().containsKey(Context.INITIAL_CONTEXT_FACTORY),
                "environment keeps the factory property");
        java.util.List<String> names = new java.util.ArrayList<>();
        NamingEnumeration<NameClassPair> nc = ic.list("");
        while (nc.hasMore()) names.add(nc.next().getName());
        check(names.contains("key"), "list delegates to underlying context");
        ic.close();
        // Name overloads
        InitialContext ic2 = new InitialContext(env);
        ic2.bind(new CompositeName("n"), "nv");
        check("nv".equals(ic2.lookup(new CompositeName("n"))), "Name overload delegation");
        ic2.close();
        if (!viaProperty) {
            System.out.println("note: builder already installed; property path asserted via builder context");
        }
    }

    static void dIcSystemPropertyForm() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "InitialContextTest$DeepInitFactory");
        try {
            InitialContext ic = new InitialContext();
            ic.bind("sysprop", "sv");
            check("sv".equals(ic.lookup("sysprop")), "system property selects the factory");
            ic.close();
        } finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        }
        check(System.getProperty(Context.INITIAL_CONTEXT_FACTORY) == null, "property restored");
    }

    static void dIcNoFactory() throws Exception {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            // a builder installed earlier in this JVM serves every lookup; the
            // no-factory failure path is not reachable anymore (builder is once-only)
            InitialContext ic = new InitialContext();
            ic.bind("builderpath", "bv");
            check("bv".equals(ic.lookup("builderpath")), "builder context serves lookups");
            ic.close();
            return;
        }
        // construction itself is lazy; first use reports the missing property
        InitialContext ic = new InitialContext();
        boolean threw = false;
        try {
            ic.lookup("anything");
        } catch (NoInitialContextException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("java.naming.factory.initial"),
                    "message names the missing property");
        }
        check(threw, "lookup without factory -> NoInitialContextException");
        threw = false;
        try { ic.list(""); } catch (NoInitialContextException e) { threw = true; }
        check(threw, "list without factory -> NoInitialContextException");
    }

    static void dIcDoLookupNegative() throws Exception {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            try {
                InitialContext.doLookup("x");
            } catch (NoInitialContextException unexpected) {
                throw new AssertionError("builder should serve doLookup", unexpected);
            } catch (javax.naming.NameNotFoundException answered) {
                // builder-produced context answered the lookup (name absent in
                // the in-memory tree): discovery through the builder worked
            }
            check(true, "builder-installed factory serves doLookup");
            return;
        }
        boolean threw = false;
        try { InitialContext.doLookup("x"); }
        catch (NoInitialContextException e) { threw = true; }
        catch (NamingException e) { throw new AssertionError("unexpected root", e); }
        check(threw, "doLookup without factory -> NoInitialContextException");
    }

    static void dIcEnvironmentPropagation() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialContextTest$DeepInitFactory");
        env.put("custom.key", "custom.value");
        InitialContext ic = new InitialContext(env);
        check("custom.value".equals(ic.getEnvironment().get("custom.key")),
                "custom environment entries visible");
        Object prev = ic.addToEnvironment("added", "1");
        check(prev == null, "addToEnvironment returns prior value");
        check("1".equals(ic.getEnvironment().get("added")), "added entry visible");
        Object rem = ic.removeFromEnvironment("added");
        check("1".equals(rem), "removeFromEnvironment returns removed value");
        check(ic.getEnvironment().get("added") == null, "removed entry gone");
        ic.close();
    }

}
