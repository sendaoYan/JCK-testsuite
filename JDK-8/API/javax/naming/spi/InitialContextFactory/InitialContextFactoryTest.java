/*
 * @test
 * @summary javax.naming.spi.InitialContextFactory conformance: interface hierarchy and members.
 * @run main InitialContextFactoryTest
 */



import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
public class InitialContextFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.InitialContextFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInitialContext"), "has method getInitialContext");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dIcfDirect();
        dIcfViaNamingManager();
        System.out.println("deep: InitialContextFactory contracts OK");
    }

    public static class DeepIcf implements InitialContextFactory {
        static int calls;
        static java.util.Hashtable<?, ?> lastEnv;
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            calls++;
            lastEnv = env;
            return new DeepIcfContext();
        }
    }

    public static class DeepIcfContext implements Context {
        public Object lookup(Name n) throws NamingException { return "V:" + n; }
        public Object lookup(String n) throws NamingException { return "V:" + n; }
        public void bind(Name n, Object o) { }
        public void bind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void unbind(Name n) { }
        public void unbind(String n) { }
        public void rename(Name o, Name n) { }
        public void rename(String o, String n) { }
        public NamingEnumeration<NameClassPair> list(Name n) { return dIcfEmptyNc(); }
        public NamingEnumeration<NameClassPair> list(String n) { return dIcfEmptyNc(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return dIcfEmptyB(); }
        public NamingEnumeration<Binding> listBindings(String n) { return dIcfEmptyB(); }
        public void destroySubcontext(Name n) { }
        public void destroySubcontext(String n) { }
        public Context createSubcontext(Name n) { return null; }
        public Context createSubcontext(String n) { return null; }
        public Object lookupLink(Name n) { return null; }
        public Object lookupLink(String n) { return null; }
        public NameParser getNameParser(Name n) { return dIcfParser; }
        public NameParser getNameParser(String n) { return dIcfParser; }
        public Name composeName(Name n, Name pfx) { return null; }
        public String composeName(String n, String pfx) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public java.util.Hashtable<?, ?> getEnvironment() { return new java.util.Hashtable<>(); }
        public void close() { }
        public String getNameInNamespace() { return "icf://root"; }
    }

    static final NameParser dIcfParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

    static NamingEnumeration<NameClassPair> dIcfEmptyNc() {
        return new NamingEnumeration<NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        };
    }

    static NamingEnumeration<Binding> dIcfEmptyB() {
        return new NamingEnumeration<Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public Binding next() { throw new java.util.NoSuchElementException(); }
            public Binding nextElement() { return next(); }
            public void close() { }
        };
    }

    static void dIcfDirect() throws Exception {
        DeepIcf f = new DeepIcf();
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put("k", "v");
        Context ctx = f.getInitialContext(env);
        check(ctx != null, "factory returns a context");
        check("V:x".equals(ctx.lookup("x")), "context usable");
        check("icf://root".equals(ctx.getNameInNamespace()), "context namespace name");
        check(f.lastEnv == env, "environment passed through");
    }

    static void dIcfViaNamingManager() throws Exception {
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            System.out.println("note: builder installed earlier; property path covered via builder");
            return;
        }
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "InitialContextFactoryTest$DeepIcf");
        Context ctx = NamingManager.getInitialContext(env);
        check(ctx instanceof DeepIcfContext, "NamingManager instantiates the configured factory");
        check(DeepIcf.calls >= 1, "factory getInitialContext invoked");
        check(DeepIcf.lastEnv == env || DeepIcf.lastEnv.equals(env), "env delivered to factory");
    }

}
