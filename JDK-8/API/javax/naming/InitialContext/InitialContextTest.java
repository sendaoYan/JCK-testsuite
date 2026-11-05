/*
 * @test
 * @summary javax.naming.InitialContext conformance: full contract coverage.
 *          lazy construction (no-arg / empty env), NoInitialContextException
 *          from lookup/getEnvironment/addToEnvironment/getNameInNamespace when
 *          no InitialContextFactory is available, eager factory instantiation
 *          failure via the env factory property, and idempotent close().
 * @run main InitialContextTest
 */

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NoInitialContextException;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.ContextNotEmptyException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;

public class InitialContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() throws Exception {
        InitialContext ctx = new InitialContext();
        check(ctx != null, "constructed");
        ctx.close();
        InitialContext ctx2 = new InitialContext(new Hashtable<Object, Object>());
        ctx2.close();
        check(true, "close with env");
    }

    static void testLazyNoFactory() throws Exception {
        final InitialContext ctx = new InitialContext();
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.lookup("x"); } },
                "lookup without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.getEnvironment(); } },
                "getEnvironment without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.addToEnvironment("k", "v"); } },
                "addToEnvironment without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.getNameInNamespace(); } },
                "getNameInNamespace without factory");
        ctx.close();
    }

    static void testCloseIdempotent() throws Exception {
        InitialContext ctx = new InitialContext();
        ctx.close();
        ctx.close();
        check(true, "close idempotent");
    }

    static void testEagerFactoryFailure() {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "no.such.Factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { new InitialContext(env); } },
                "uninstantiable factory -> NoInitialContextException at construction");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testLazyNoFactory();
        testCloseIdempotent();
        testEagerFactoryFailure();
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
