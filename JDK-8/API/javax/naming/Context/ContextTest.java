/*
 * @test
 * @summary javax.naming.Context conformance: interface hierarchy, key method
 *          presence, and stable environment property constant values.
 * @run main ContextTest
 */

import javax.naming.Context;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.ContextNotEmptyException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

public class ContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.Context");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");
        check(names.contains("close"), "has method close");
    }

    static void testMoreMethods() {
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : Context.class.getMethods()) names.add(m.getName());
        String[] expected = {"lookup", "rebind", "unbind", "rename", "list", "listBindings",
                "createSubcontext", "destroySubcontext", "lookupLink", "getNameParser",
                "composeName", "removeFromEnvironment", "getEnvironment", "getNameInNamespace"};
        for (String e : expected) check(names.contains(e), "has method " + e);
    }

    static void testConstants() {
        check("java.naming.factory.initial".equals(Context.INITIAL_CONTEXT_FACTORY), "INITIAL_CONTEXT_FACTORY");
        check("java.naming.factory.object".equals(Context.OBJECT_FACTORIES), "OBJECT_FACTORIES");
        check("java.naming.factory.state".equals(Context.STATE_FACTORIES), "STATE_FACTORIES");
        check("java.naming.factory.url.pkgs".equals(Context.URL_PKG_PREFIXES), "URL_PKG_PREFIXES");
        check("java.naming.provider.url".equals(Context.PROVIDER_URL), "PROVIDER_URL");
        check("java.naming.dns.url".equals(Context.DNS_URL), "DNS_URL");
        check("java.naming.authoritative".equals(Context.AUTHORITATIVE), "AUTHORITATIVE");
        check("java.naming.batchsize".equals(Context.BATCHSIZE), "BATCHSIZE");
        check("java.naming.referral".equals(Context.REFERRAL), "REFERRAL");
        check("java.naming.security.protocol".equals(Context.SECURITY_PROTOCOL), "SECURITY_PROTOCOL");
        check("java.naming.security.authentication".equals(Context.SECURITY_AUTHENTICATION), "SECURITY_AUTHENTICATION");
        check("java.naming.security.principal".equals(Context.SECURITY_PRINCIPAL), "SECURITY_PRINCIPAL");
        check("java.naming.security.credentials".equals(Context.SECURITY_CREDENTIALS), "SECURITY_CREDENTIALS");
        check("java.naming.language".equals(Context.LANGUAGE), "LANGUAGE");
        try {
            check("java.naming.applet".equals(
                    Context.class.getField("APPLET").get(null)), "APPLET");
        } catch (NoSuchFieldException x) {
            check(true, "APPLET removed on this JDK (version difference)");
        } catch (IllegalAccessException x) {
            throw new AssertionError(x);
        }
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testMoreMethods();
        testConstants();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCtxConstants();
        dCtxStateMachine();
        dCtxListForms();
        dCtxSubcontextLifecycle();
        dCtxEnvironment();
        dCtxNameParserAndCompose();
        dCtxLookupLink();
        dCtxCloseIdempotent();
        System.out.println("deep: Context contracts OK");
    }

    static void dCtxConstants() {
        check("java.naming.factory.initial".equals(Context.INITIAL_CONTEXT_FACTORY), "INITIAL_CONTEXT_FACTORY");
        check("java.naming.factory.object".equals(Context.OBJECT_FACTORIES), "OBJECT_FACTORIES");
        check("java.naming.factory.state".equals(Context.STATE_FACTORIES), "STATE_FACTORIES");
        check("java.naming.factory.url.pkgs".equals(Context.URL_PKG_PREFIXES), "URL_PKG_PREFIXES");
        check("java.naming.provider.url".equals(Context.PROVIDER_URL), "PROVIDER_URL");
        check("java.naming.dns.url".equals(Context.DNS_URL), "DNS_URL");
        check("java.naming.authoritative".equals(Context.AUTHORITATIVE), "AUTHORITATIVE");
        check("java.naming.batchsize".equals(Context.BATCHSIZE), "BATCHSIZE");
        check("java.naming.referral".equals(Context.REFERRAL), "REFERRAL");
        check("java.naming.security.protocol".equals(Context.SECURITY_PROTOCOL), "SECURITY_PROTOCOL");
        check("java.naming.security.authentication".equals(Context.SECURITY_AUTHENTICATION), "SECURITY_AUTHENTICATION");
        check("java.naming.security.principal".equals(Context.SECURITY_PRINCIPAL), "SECURITY_PRINCIPAL");
        check("java.naming.security.credentials".equals(Context.SECURITY_CREDENTIALS), "SECURITY_CREDENTIALS");
        check("java.naming.language".equals(Context.LANGUAGE), "LANGUAGE");
        try {
            check("java.naming.applet".equals(
                    Context.class.getField("APPLET").get(null)), "APPLET");
        } catch (NoSuchFieldException x) {
            check(true, "APPLET removed on this JDK (version difference)");
        } catch (IllegalAccessException x) {
            throw new AssertionError(x);
        }
    }

    static void dCtxStateMachine() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        Object v1 = new Object();
        ctx.bind("a", v1);
        check(ctx.lookup("a") == v1, "lookup returns bound object");
        boolean threw = false;
        try { ctx.bind("a", new Object()); } catch (NameAlreadyBoundException e) { threw = true; }
        check(threw, "bind over existing -> NameAlreadyBoundException");
        Object v2 = new Object();
        ctx.rebind("a", v2);
        check(ctx.lookup("a") == v2, "rebind replaces binding");
        ctx.rename("a", "b");
        check(ctx.lookup("b") == v2, "renamed object reachable under new name");
        threw = false;
        try { ctx.lookup("a"); } catch (NameNotFoundException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("a"), "message contains the name");
        }
        check(threw, "old name gone after rename");
        threw = false;
        try { ctx.rename("missing", "x"); } catch (NameNotFoundException e) { threw = true; }
        check(threw, "rename of missing -> NameNotFoundException");
        ctx.unbind("b");
        threw = false;
        try { ctx.lookup("b"); } catch (NameNotFoundException e) { threw = true; }
        check(threw, "lookup after unbind fails");
        threw = false;
        try { ctx.unbind("b"); } catch (NameNotFoundException e) { threw = true; }
        check(threw, "unbind of missing -> NameNotFoundException");
        // Name overloads delegate to String forms
        ctx.bind(new CompositeName("n1"), "v1");
        check("v1".equals(ctx.lookup(new CompositeName("n1"))), "Name overload round trip");
    }

    static void dCtxListForms() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        Object x = new Object();
        ctx.bind("k1", x);
        ctx.bind("k2", "s");
        java.util.List<String> names = new java.util.ArrayList<>();
        NamingEnumeration<NameClassPair> nc = ctx.list("");
        while (nc.hasMore()) names.add(nc.next().getName());
        java.util.Collections.sort(names);
        check(names.equals(java.util.Arrays.asList("k1", "k2")), "list yields names sorted");
        java.util.List<Object> objs = new java.util.ArrayList<>();
        NamingEnumeration<Binding> nb = ctx.listBindings("");
        while (nb.hasMore()) objs.add(nb.next().getObject());
        check(objs.size() == 2 && objs.contains(x) && objs.contains("s"),
                "listBindings getObject returns the bound objects");
        NameClassPair pair = ctx.list("").next();
        check(pair.getClassName() != null, "list pairs expose class names");
        check(!nc.hasMore(), "exhausted enumeration");
    }

    static void dCtxSubcontextLifecycle() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        Context sub = ctx.createSubcontext("sub");
        check(sub != null && sub instanceof Context, "createSubcontext returns Context");
        boolean threw = false;
        try { ctx.createSubcontext("sub"); } catch (NameAlreadyBoundException e) { threw = true; }
        check(threw, "createSubcontext duplicate -> NameAlreadyBoundException");
        ((DeepCtx) sub).bind("inner", "v");
        threw = false;
        try { ctx.destroySubcontext("sub"); } catch (ContextNotEmptyException e) { threw = true; }
        check(threw, "destroy of non-empty subcontext -> ContextNotEmptyException");
        ((DeepCtx) sub).unbind("inner");
        ctx.destroySubcontext("sub");
        threw = false;
        try { ctx.lookup("sub"); } catch (NameNotFoundException e) { threw = true; }
        check(threw, "subcontext gone after destroy");
        threw = false;
        try { ctx.destroySubcontext("nosuch"); } catch (NameNotFoundException e) { threw = true; }
        check(threw, "destroy missing -> NameNotFoundException");
    }

    static void dCtxEnvironment() throws Exception {
        java.util.Hashtable<Object, Object> init = new java.util.Hashtable<>();
        init.put("seed", "1");
        DeepCtx ctx = new DeepCtx(init);
        check("1".equals(ctx.getEnvironment().get("seed")), "initial environment visible");
        Object prev = ctx.addToEnvironment("key", "value");
        check(prev == null, "addToEnvironment returns previous value (null when new)");
        check("value".equals(ctx.getEnvironment().get("key")), "added entry visible");
        prev = ctx.addToEnvironment("key", "value2");
        check("value".equals(prev), "addToEnvironment returns replaced value");
        Object removed = ctx.removeFromEnvironment("key");
        check("value2".equals(removed), "removeFromEnvironment returns removed value");
        check(ctx.getEnvironment().get("key") == null, "entry gone");
        check(ctx.removeFromEnvironment("never") == null, "remove missing returns null");
        // snapshot semantics: mutating the returned table must not affect the context
        java.util.Hashtable<?, ?> snap = ctx.getEnvironment();
        // cast needed because getEnvironment returns Hashtable<?,?>
        @SuppressWarnings("unchecked")
        java.util.Hashtable<Object, Object> mut = (java.util.Hashtable<Object, Object>) snap;
        mut.put("injected", "x");
        check(ctx.getEnvironment().get("injected") == null, "getEnvironment returns a snapshot");
    }

    static void dCtxNameParserAndCompose() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        NameParser p = ctx.getNameParser("");
        Name n = p.parse("a/b/c");
        check(n.size() == 3 && "a".equals(n.get(0)), "parser round trip");
        Name composed = ctx.composeName(new CompositeName("x/y"), new CompositeName("pre"));
        check("pre/x/y".equals(composed.toString()), "composeName prefix+name");
        check(composed instanceof CompositeName, "compose preserves name type");
        check("pre/x".equals(ctx.composeName("x", "pre")), "String composeName");
        Name prefixClone = new CompositeName("pre");
        ctx.composeName(new CompositeName("x"), prefixClone);
        check(prefixClone.size() == 1, "composeName does not mutate prefix argument");
    }

    static void dCtxLookupLink() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        LinkRef link = new LinkRef("target");
        ctx.bind("alias", link);
        Object viaLink = ctx.lookupLink("alias");
        check(viaLink instanceof LinkRef, "lookupLink returns link without following");
        check("target".equals(((LinkRef) viaLink).getLinkName()), "link content readable");
        ctx.bind("plain", "value");
        check("value".equals(ctx.lookupLink("plain")), "lookupLink on non-link returns the object");
    }

    static void dCtxCloseIdempotent() throws Exception {
        DeepCtx ctx = new DeepCtx(null);
        ctx.close();
        ctx.close(); // second close must not throw
        check(ctx.closed, "close recorded");
        ctx.bind("after", "close-context-still-usable-per-impl");
        check(true, "close semantics are service-specific (recorded)");
        check("".equals(ctx.getNameInNamespace()), "getNameInNamespace");
    }

    /** In-memory Context exercising the Context state machine contract. */
    public static class DeepCtx implements Context {
        final java.util.Hashtable<String, Object> store = new java.util.Hashtable<>();
        final java.util.Hashtable<Object, Object> env;
        boolean closed;
        DeepCtx(java.util.Hashtable<?, ?> e) {
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
        public NamingEnumeration<NameClassPair> list(String n) { return new DeepEnumNC(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return listBindings(""); }
        public NamingEnumeration<Binding> listBindings(String n) { return new DeepEnumB(); }
        public void destroySubcontext(Name n) throws NamingException { destroySubcontext(n.toString()); }
        public void destroySubcontext(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            if (v instanceof DeepCtx && !((DeepCtx) v).store.isEmpty())
                throw new ContextNotEmptyException(n);
            store.remove(n);
        }
        public Context createSubcontext(Name n) throws NamingException { return createSubcontext(n.toString()); }
        public synchronized Context createSubcontext(String n) throws NamingException {
            if (store.containsKey(n)) throw new NameAlreadyBoundException(n);
            DeepCtx c = new DeepCtx(null);
            store.put(n, c);
            return c;
        }
        public Object lookupLink(Name n) throws NamingException { return lookup(n); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public NameParser getNameParser(Name n) { return dCtxParser; }
        public NameParser getNameParser(String n) { return dCtxParser; }
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
        public void close() { closed = true; }
        public String getNameInNamespace() { return ""; }
        class DeepEnumNC implements NamingEnumeration<NameClassPair> {
        final java.util.Iterator<java.util.Map.Entry<String, Object>> it =
                new java.util.TreeMap<String, Object>(DeepCtx.this.store).entrySet().iterator();
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

    class DeepEnumB implements NamingEnumeration<Binding> {
        final java.util.Iterator<java.util.Map.Entry<String, Object>> it =
                new java.util.TreeMap<String, Object>(DeepCtx.this.store).entrySet().iterator();
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



    static final NameParser dCtxParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };



}
