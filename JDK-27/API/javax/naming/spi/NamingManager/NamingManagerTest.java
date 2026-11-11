/*
 * @test
 * @summary javax.naming.spi.NamingManager conformance: white-box structure + black-box behavior.
 * @run main/othervm NamingManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NoInitialContextException;
import javax.naming.spi.NamingManager;
import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.StateFactory;

public class NamingManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.NamingManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getObjectInstance"), "getObjectInstance");
        check(hasMethod(c, "getStateToBind"), "getStateToBind");
        check(hasMethod(c, "getInitialContext"), "getInitialContext");
        check(hasMethod(c, "hasInitialContextFactoryBuilder"), "hasInitialContextFactoryBuilder");
        check(hasMethod(c, "setInitialContextFactoryBuilder"), "setInitialContextFactoryBuilder");
        check(hasMethod(c, "setObjectFactoryBuilder"), "setObjectFactoryBuilder");
        check(hasMethod(c, "getContinuationContext"), "getContinuationContext");
        check(hasMethod(c, "getURLContext"), "getURLContext");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Documented CPE constant.
        check("java.naming.spi.CannotProceedException".equals(NamingManager.CPE), "CPE constant value");

        // No builder installed by default in a fresh JVM.
        check(!NamingManager.hasInitialContextFactoryBuilder(), "no initial context factory builder");

        // getObjectInstance: ordinary object is returned unchanged when no factory applies.
        String plain = "plain-object";
        check(NamingManager.getObjectInstance(plain, null, null, null) == plain,
                "getObjectInstance returns plain object unchanged");
        check(NamingManager.getObjectInstance(null, null, null, null) == null,
                "getObjectInstance(null) returns null");

        // getStateToBind: ordinary object is returned unchanged when no state factory applies.
        check(NamingManager.getStateToBind(plain, null, null, null) == plain,
                "getStateToBind returns plain object unchanged");

        // getInitialContext with no configured factory throws NoInitialContextException.
        boolean threw = false;
        try {
            NamingManager.getInitialContext(new Hashtable<>());
        } catch (NoInitialContextException expected) {
            threw = true;
        }
        check(threw, "getInitialContext(empty) throws NoInitialContextException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNmInitialContextDelegation();
        dNmObjectInstance();
        dNmStateToBind();
        dNmUrlContext();
        dNmBuilderOnceOnly();
        System.out.println("deep: NamingManager contracts OK");
    }

    public static class DeepNmFactory implements InitialContextFactory {
        public static java.util.List<java.util.Hashtable<?, ?>> calls = new java.util.ArrayList<>();
        public Context getInitialContext(java.util.Hashtable<?, ?> env) {
            calls.add(env);
            return new DeepNmContext(env);
        }
    }

    public static class DeepNmContext implements Context {
        final java.util.Hashtable<Object, Object> env;
        DeepNmContext(java.util.Hashtable<?, ?> e) {
            env = (e == null) ? new java.util.Hashtable<Object, Object>()
                    : new java.util.Hashtable<Object, Object>(e);
        }
        public Object lookup(Name n) throws NamingException { return "LOOKUP:" + n; }
        public Object lookup(String n) throws NamingException { return "LOOKUP:" + n; }
        public void bind(Name n, Object o) { }
        public void bind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void unbind(Name n) { }
        public void unbind(String n) { }
        public void rename(Name o, Name n) { }
        public void rename(String o, String n) { }
        public NamingEnumeration<NameClassPair> list(Name n) { return dNmEmptyNc(); }
        public NamingEnumeration<NameClassPair> list(String n) { return dNmEmptyNc(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return dNmEmptyB(); }
        public NamingEnumeration<Binding> listBindings(String n) { return dNmEmptyB(); }
        public void destroySubcontext(Name n) { }
        public void destroySubcontext(String n) { }
        public Context createSubcontext(Name n) { return null; }
        public Context createSubcontext(String n) { return null; }
        public Object lookupLink(Name n) { return null; }
        public Object lookupLink(String n) { return null; }
        public NameParser getNameParser(Name n) { return dNmParser; }
        public NameParser getNameParser(String n) { return dNmParser; }
        public Name composeName(Name n, Name pfx) { return null; }
        public String composeName(String n, String pfx) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public java.util.Hashtable<?, ?> getEnvironment() { return (java.util.Hashtable<?, ?>) env.clone(); }
        public void close() { }
        public String getNameInNamespace() { return ""; }
    }

    static final NameParser dNmParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

    static NamingEnumeration<NameClassPair> dNmEmptyNc() {
        return new NamingEnumeration<NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        };
    }

    static NamingEnumeration<Binding> dNmEmptyB() {
        return new NamingEnumeration<Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public Binding next() { throw new java.util.NoSuchElementException(); }
            public Binding nextElement() { return next(); }
            public void close() { }
        };
    }

    public static class DeepNmObjFactory implements ObjectFactory {
        public static Object lastObj;
        public static Name lastName;
        public static Context lastCtx;
        public static java.util.Hashtable<?, ?> lastEnv;
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj; lastName = name; lastCtx = nameCtx; lastEnv = environment;
            return "FROM-OBJECT-FACTORY";
        }
    }

    public static class DeepNmNullFactory implements ObjectFactory {
        public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            return null;
        }
    }

    public static class DeepNmStateFactory implements StateFactory {
        public static Object lastObj;
        public Object getStateToBind(Object obj, Name name, Context nameCtx,
                java.util.Hashtable<?, ?> environment) {
            lastObj = obj;
            return "STATE-FROM-FACTORY:" + obj;
        }
    }

    static void dNmInitialContextDelegation() throws Exception {
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "NamingManagerTest$DeepNmFactory");
        Context ctx = NamingManager.getInitialContext(env);
        check(ctx instanceof DeepNmContext, "getInitialContext delegates to the named factory");
        check("LOOKUP:a/b".equals(ctx.lookup("a/b")), "delegated context is live");
        check(!DeepNmFactory.calls.isEmpty(), "factory received the environment");
        // empty environment -> NoInitialContextException naming the property
        boolean threw = false;
        try {
            NamingManager.getInitialContext(new java.util.Hashtable<Object, Object>());
        } catch (NoInitialContextException e) {
            threw = true;
            check(e.getMessage().contains("java.naming.factory.initial"), "message names property");
        }
        check(threw, "no factory property -> NoInitialContextException");
        // unloadable factory class -> NoInitialContextException with root cause
        threw = false;
        java.util.Hashtable<Object, Object> bad = new java.util.Hashtable<>();
        bad.put(Context.INITIAL_CONTEXT_FACTORY, "no.such.InitialContextFactory");
        try {
            NamingManager.getInitialContext(bad);
        } catch (NoInitialContextException e) {
            threw = true;
            check(e.getMessage().contains("no.such.InitialContextFactory"), "message names the class");
            check(e.getRootCause() != null, "root cause attached");
        }
        check(threw, "unloadable factory -> NoInitialContextException");
    }

    static void dNmObjectInstance() throws Exception {
        // null reference is a documented short circuit
        Object out = NamingManager.getObjectInstance(null, new CompositeName("n"), null,
                new java.util.Hashtable<Object, Object>());
        check(out == null, "getObjectInstance(null, ...) returns null directly");
        // no factory configured -> original reference returned
        Object sent = new Object();
        out = NamingManager.getObjectInstance(sent, new CompositeName("n"), null,
                new java.util.Hashtable<Object, Object>());
        check(out == sent, "no factory -> original object");
        // Reference without factory -> the Reference itself
        Reference ref = new Reference("SomeClass");
        out = NamingManager.getObjectInstance(ref, new CompositeName("n"), null,
                new java.util.Hashtable<Object, Object>());
        check(out == ref, "Reference without factory -> the Reference");
        // factory via OBJECT_FACTORIES environment property
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "NamingManagerTest$DeepNmObjFactory");
        Name nm = new CompositeName("the/name");
        DeepNmContext ctx = new DeepNmContext(null);
        out = NamingManager.getObjectInstance(sent, nm, ctx, env);
        check("FROM-OBJECT-FACTORY".equals(out), "env factory result");
        check(DeepNmObjFactory.lastObj == sent, "factory receives the reference");
        check(DeepNmObjFactory.lastName.equals(nm), "factory receives the name");
        check(DeepNmObjFactory.lastCtx == ctx, "factory receives the context");
        check(DeepNmObjFactory.lastEnv == env || DeepNmObjFactory.lastEnv.equals(env),
                "factory receives the environment");
        // factory returning null -> original object
        env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "NamingManagerTest$DeepNmNullFactory");
        out = NamingManager.getObjectInstance(sent, new CompositeName("n"), null, env);
        check(out == sent, "null-returning factory -> original object");
        // unknown factory class -> original object (missing classes skipped)
        env = new java.util.Hashtable<>();
        env.put(Context.OBJECT_FACTORIES, "no.such.ObjectFactory");
        out = NamingManager.getObjectInstance(sent, new CompositeName("n"), null, env);
        check(out == sent, "missing factory class skipped");
    }

    static void dNmStateToBind() throws Exception {
        // no factory -> original object
        Object obj = "plain";
        Object out = NamingManager.getStateToBind(obj, new CompositeName("n"), null,
                new java.util.Hashtable<Object, Object>());
        check(out == obj, "no state factory -> original object");
        // factory via STATE_FACTORIES
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(Context.STATE_FACTORIES, "NamingManagerTest$DeepNmStateFactory");
        out = NamingManager.getStateToBind(obj, new CompositeName("n"), null, env);
        check("STATE-FROM-FACTORY:plain".equals(out), "state factory replaces the object");
        check(DeepNmStateFactory.lastObj == obj, "state factory receives the object");
    }

    static void dNmUrlContext() throws Exception {
        // no URL context factories installed -> null
        Context url = NamingManager.getURLContext("http", new java.util.Hashtable<Object, Object>());
        check(url == null, "no built-in http scheme -> null");
        // the java.naming module ships URL context factories for dns and ldap
        url = NamingManager.getURLContext("dns", new java.util.Hashtable<Object, Object>());
        check(url != null, "built-in dns URL context factory discovered");
        url = NamingManager.getURLContext("ldap", new java.util.Hashtable<Object, Object>());
        check(url != null, "built-in ldap URL context factory discovered");
    }

    static void dNmBuilderOnceOnly() throws Exception {
        check(!NamingManager.hasInitialContextFactoryBuilder(), "no builder initially");
        NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {
            public InitialContextFactory createInitialContextFactory(java.util.Hashtable<?, ?> env) {
                return new DeepNmFactory();
            }
        });
        check(NamingManager.hasInitialContextFactoryBuilder(), "builder installed");
        // builder-produced factory serves getInitialContext
        Context ctx = NamingManager.getInitialContext(new java.util.Hashtable<Object, Object>());
        check(ctx instanceof DeepNmContext, "getInitialContext goes through the builder");
        // a second set attempt is rejected (once-only per JVM)
        boolean ise = false;
        try { NamingManager.setInitialContextFactoryBuilder(null); }
        catch (IllegalStateException e) {
            ise = true;
            check("InitialContextFactoryBuilder already set".equals(e.getMessage()),
                    "IllegalStateException message");
        }
        check(ise, "builder can only be set once");
    }

}
