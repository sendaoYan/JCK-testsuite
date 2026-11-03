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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
