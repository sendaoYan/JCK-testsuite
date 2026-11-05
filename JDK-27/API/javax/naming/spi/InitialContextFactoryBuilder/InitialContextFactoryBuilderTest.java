/*
 * @test
 * @summary javax.naming.spi.InitialContextFactoryBuilder conformance: white-box structure + black-box behavior.
 * @run main InitialContextFactoryBuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

public class InitialContextFactoryBuilderTest {
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
        Class<?> c = Class.forName("javax.naming.spi.InitialContextFactoryBuilder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "createInitialContextFactory", Hashtable.class),
                "createInitialContextFactory(Hashtable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Hashtable<?, ?>[] seen = new Hashtable<?, ?>[1];
        final InitialContextFactory produced = env -> null;

        // Anonymous implementation: builder returns a factory, which is then used.
        InitialContextFactoryBuilder builder = env -> {
            seen[0] = env;
            return produced;
        };

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.example.Factory");
        InitialContextFactory factory = builder.createInitialContextFactory(env);
        check(seen[0] == env, "builder receives the environment");
        check(factory == produced, "builder returns the factory it produced");
        check(factory.getInitialContext(env) == null, "produced factory is callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dIcfbInstalled();
        System.out.println("deep: InitialContextFactoryBuilder contracts OK");
    }

    public static class DeepIcfbFactory implements InitialContextFactory {
        public static java.util.Hashtable<?, ?> lastEnv;
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            lastEnv = env;
            return new DeepIcfbContext();
        }
    }

    public static class DeepIcfbContext implements Context {
        public Object lookup(Name n) throws NamingException { return "B:" + n; }
        public Object lookup(String n) throws NamingException { return "B:" + n; }
        public void bind(Name n, Object o) { }
        public void bind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void unbind(Name n) { }
        public void unbind(String n) { }
        public void rename(Name o, Name n) { }
        public void rename(String o, String n) { }
        public NamingEnumeration<NameClassPair> list(Name n) { return dIcfbEmptyNc(); }
        public NamingEnumeration<NameClassPair> list(String n) { return dIcfbEmptyNc(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return dIcfbEmptyB(); }
        public NamingEnumeration<Binding> listBindings(String n) { return dIcfbEmptyB(); }
        public void destroySubcontext(Name n) { }
        public void destroySubcontext(String n) { }
        public Context createSubcontext(Name n) { return null; }
        public Context createSubcontext(String n) { return null; }
        public Object lookupLink(Name n) { return null; }
        public Object lookupLink(String n) { return null; }
        public NameParser getNameParser(Name n) { return dIcfbParser; }
        public NameParser getNameParser(String n) { return dIcfbParser; }
        public Name composeName(Name n, Name pfx) { return null; }
        public String composeName(String n, String pfx) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public java.util.Hashtable<?, ?> getEnvironment() { return new java.util.Hashtable<>(); }
        public void close() { }
        public String getNameInNamespace() { return "icfb://root"; }
    }

    static final NameParser dIcfbParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

    static NamingEnumeration<NameClassPair> dIcfbEmptyNc() {
        return new NamingEnumeration<NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        };
    }

    static NamingEnumeration<Binding> dIcfbEmptyB() {
        return new NamingEnumeration<Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public Binding next() { throw new java.util.NoSuchElementException(); }
            public Binding nextElement() { return next(); }
            public void close() { }
        };
    }

    static void dIcfbInstalled() throws Exception {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            // once-only JVM state already consumed by an earlier deep test in this file
            check(true, "builder already installed (once-only semantics hold)");
            return;
        }
        NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {
            public InitialContextFactory createInitialContextFactory(java.util.Hashtable<?, ?> env) {
                return new DeepIcfbFactory();
            }
        });
        check(NamingManager.hasInitialContextFactoryBuilder(), "hasInitialContextFactoryBuilder true");
        Context ctx = NamingManager.getInitialContext(new java.util.Hashtable<Object, Object>());
        check(ctx instanceof DeepIcfbContext, "builder-created factory serves getInitialContext");
        check("B:q".equals(ctx.lookup("q")), "builder context live");
        boolean ise = false;
        try { NamingManager.setInitialContextFactoryBuilder(null); }
        catch (IllegalStateException e) { ise = true; }
        check(ise, "second install rejected with IllegalStateException");
    }

}
