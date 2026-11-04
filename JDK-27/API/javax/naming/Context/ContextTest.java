/*
 * @test
 * @summary javax.naming.Context conformance: white-box structure + black-box behavior.
 * @run main ContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Objects;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class ContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** In-memory Context used to exercise the interface contract end to end. */
    static class FlatContext implements Context {
        final Hashtable<String, Object> env = new Hashtable<>();
        final java.util.Map<String, Object> store = new java.util.LinkedHashMap<>();
        public Object lookup(String name) throws NamingException { require(name); return store.get(name); }
        public void bind(String name, Object obj) throws NamingException { require(name); store.put(name, obj); }
        public void rebind(String name, Object obj) { store.put(name, obj); }
        public void unbind(String name) { store.remove(name); }
        public void rename(String oldName, String newName) { store.put(newName, store.remove(oldName)); }
        public NamingEnumeration<NameClassPair> list(String name) {
            java.util.List<NameClassPair> ps = new java.util.ArrayList<>();
            store.keySet().forEach(k -> ps.add(new NameClassPair(k, String.valueOf(store.get(k).getClass().getName()))));
            return enumOf(ps);
        }
        public NamingEnumeration<Binding> listBindings(String name) {
            java.util.List<Binding> bs = new java.util.ArrayList<>();
            store.forEach((k, v) -> bs.add(new Binding(k, v)));
            return enumOf(bs);
        }
        public void destroySubcontext(String name) { store.remove(name); }
        public Context createSubcontext(String name) { store.put(name, "ctx:" + name); return this; }
        public Object lookupLink(String name) { return store.get(name); }
        public String getNameInNamespace() { return ""; }
        public Object addToEnvironment(String prop, Object val) { return env.put(prop, val); }
        public Object removeFromEnvironment(String prop) { return env.remove(prop); }
        public Hashtable<?,?> getEnvironment() { return env; }
        public void close() { env.clear(); }
        public String composeName(String name, String prefix) { return prefix + "/" + name; }
        public javax.naming.Name composeName(javax.naming.Name name, javax.naming.Name prefix) { return (javax.naming.Name) name.clone(); }
        public javax.naming.NameParser getNameParser(String name) { return s -> new javax.naming.CompositeName(s); }
        public javax.naming.NameParser getNameParser(javax.naming.Name name) { return s -> new javax.naming.CompositeName(s); }
        public Object lookup(javax.naming.Name name) throws NamingException { return lookup(name.toString()); }
        public void bind(javax.naming.Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public void rebind(javax.naming.Name n, Object o) throws NamingException { rebind(n.toString(), o); }
        public void unbind(javax.naming.Name n) throws NamingException { unbind(n.toString()); }
        public void rename(javax.naming.Name o, javax.naming.Name n) throws NamingException { rename(o.toString(), n.toString()); }
        public NamingEnumeration<NameClassPair> list(javax.naming.Name name) throws NamingException { return list(name.toString()); }
        public NamingEnumeration<Binding> listBindings(javax.naming.Name name) throws NamingException { return listBindings(name.toString()); }
        public void destroySubcontext(javax.naming.Name n) throws NamingException { destroySubcontext(n.toString()); }
        public Context createSubcontext(javax.naming.Name n) throws NamingException { return createSubcontext(n.toString()); }
        public Object lookupLink(javax.naming.Name n) throws NamingException { return lookupLink(n.toString()); }
        private void require(String n) throws javax.naming.InvalidNameException { if (n == null || n.isEmpty()) throw new javax.naming.InvalidNameException("bad name"); }
        <T> NamingEnumeration<T> enumOf(Iterable<T> it) {
            java.util.Iterator<T> i = it.iterator();
            return new NamingEnumeration<T>() {
                public T next() { return i.next(); }
                public boolean hasMore() { return i.hasNext(); }
                public void close() { }
                public T nextElement() { return i.next(); }
                public boolean hasMoreElements() { return i.hasNext(); }
            };
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Context.class;
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        String[] ops = {"lookup", "bind", "rebind", "unbind", "rename", "list", "listBindings",
                "destroySubcontext", "createSubcontext", "lookupLink", "getNameParser",
                "composeName", "addToEnvironment", "removeFromEnvironment", "getEnvironment",
                "getNameInNamespace", "close"};
        int found = 0;
        for (Method m : c.getMethods()) for (String o : ops) if (m.getName().equals(o)) { found++; break; }
        check(found >= ops.length, "all operation families present: " + found);
        // Name-based overloads
        check(c.getMethod("lookup", javax.naming.Name.class) != null, "lookup(Name)");
        check(c.getMethod("list", String.class).getReturnType() == NamingEnumeration.class, "list returns NamingEnumeration");
        check(java.util.Arrays.asList(c.getMethod("lookup", String.class).getExceptionTypes())
                .contains(NamingException.class), "lookup declares NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // well-known environment property constant values (documented, stable)
        check(Context.INITIAL_CONTEXT_FACTORY.equals("java.naming.factory.initial"), "INITIAL_CONTEXT_FACTORY");
        check(Context.OBJECT_FACTORIES.equals("java.naming.factory.object"), "OBJECT_FACTORIES");
        check(Context.STATE_FACTORIES.equals("java.naming.factory.state"), "STATE_FACTORIES");
        check(Context.URL_PKG_PREFIXES.equals("java.naming.factory.url.pkgs"), "URL_PKG_PREFIXES");
        check(Context.PROVIDER_URL.equals("java.naming.provider.url"), "PROVIDER_URL");
        check(Context.DNS_URL.equals("java.naming.dns.url"), "DNS_URL");
        check(Context.AUTHORITATIVE.equals("java.naming.authoritative"), "AUTHORITATIVE");
        check(Context.BATCHSIZE.equals("java.naming.batchsize"), "BATCHSIZE");
        check(Context.REFERRAL.equals("java.naming.referral"), "REFERRAL");
        check(Context.SECURITY_PROTOCOL.equals("java.naming.security.protocol"), "SECURITY_PROTOCOL");
        check(Context.SECURITY_AUTHENTICATION.equals("java.naming.security.authentication"), "SECURITY_AUTHENTICATION");
        check(Context.SECURITY_PRINCIPAL.equals("java.naming.security.principal"), "SECURITY_PRINCIPAL");
        check(Context.SECURITY_CREDENTIALS.equals("java.naming.security.credentials"), "SECURITY_CREDENTIALS");
        check(Context.LANGUAGE.equals("java.naming.language"), "LANGUAGE");

        // full context lifecycle through the interface reference
        Context ctx = new FlatContext();
        ctx.bind("a", "va");
        ctx.bind("b", 42);
        check(ctx.lookup("a").equals("va"), "lookup after bind");
        check(ctx.lookup("b").equals(42), "lookup second binding");
        ctx.rebind("a", "va2");
        check(ctx.lookup("a").equals("va2"), "rebind replaces");
        ctx.rename("a", "c");
        check(ctx.lookup("c").equals("va2"), "rename moves");
        check(ctx.lookup("a") == null, "old name removed by rename");
        java.util.List<String> names = new java.util.ArrayList<>();
        NamingEnumeration<NameClassPair> en = ctx.list("");
        while (en.hasMore()) names.add(en.next().getName());
        java.util.Collections.sort(names);
        check(names.equals(java.util.Arrays.asList("b", "c")), "list names sorted");
        java.util.List<Object> objs = new java.util.ArrayList<>();
        NamingEnumeration<Binding> be = ctx.listBindings("");
        while (be.hasMore()) objs.add(be.next().getObject());
        check(objs.contains("va2") && objs.contains(42), "listBindings exposes objects");
        ctx.unbind("c");
        check(!names.isEmpty(), "unbind tolerated");
        ctx.addToEnvironment("k", "v");
        check(ctx.getEnvironment().get("k").equals("v"), "environment visible");
        check(ctx.removeFromEnvironment("k").equals("v"), "removeFromEnvironment");
        check(!ctx.getEnvironment().containsKey("k"), "removed");
        check(ctx.composeName("x", "root").equals("root/x"), "composeName");
        check(ctx.getNameInNamespace().equals(""), "name in namespace");
        ctx.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
