/*
 * @test
 * @summary javax.naming.Referenceable conformance: interface hierarchy and members.
 * @run main ReferenceableTest
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
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
public class ReferenceableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.Referenceable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getReference"), "has method getReference");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dRefableDirect();
        dRefableViaContext();
        System.out.println("deep: Referenceable contracts OK");
    }

    static final class DeepReferenceable implements Referenceable {
        final Reference ref;
        int calls;
        DeepReferenceable(Reference ref) { this.ref = ref; }
        public Reference getReference() { calls++; return ref; }
    }

    static void dRefableDirect() throws Exception {
        Reference preset = new Reference("MyClass", new StringRefAddr("k", "v"), "Fac", "loc");
        DeepReferenceable r = new DeepReferenceable(preset);
        check(r.getReference() == preset, "getReference returns the exact reference");
        check(r.getReference() == preset, "stable across calls");
        check(r.calls == 2, "getReference invoked each time");
        // naming service convention: Referenceable used to obtain a Reference for binding
        Reference obtained = r.getReference();
        check("MyClass".equals(obtained.getClassName()) && "Fac".equals(obtained.getFactoryClassName()),
                "reference details preserved");
    }

    static void dRefableViaContext() throws Exception {
        DeepRefContext ctx = new DeepRefContext();
        Reference preset = new Reference("RefClass", new StringRefAddr("k", "v"));
        DeepReferenceable r = new DeepReferenceable(preset);
        ctx.bind("bound", r);
        check(ctx.store.get("bound") == r, "context stores the object itself");
        // simulate the Referenceable lookup convention: getObjectInstance on a Referenceable target
        Object got = javax.naming.spi.NamingManager.getObjectInstance(r, new CompositeName("bound"), ctx,
                new java.util.Hashtable<Object, Object>());
        check(got == r, "getObjectInstance returns the original when no factory handles it");
        Reference fromLookup = ((DeepReferenceable) ctx.lookup("bound")).getReference();
        check(fromLookup == preset, "reference obtainable through context round trip");
    }

    static final class DeepRefContext implements Context {
        final java.util.Map<String, Object> store = new java.util.Hashtable<>();
        public Object lookup(Name n) throws NamingException { return lookup(n.toString()); }
        public Object lookup(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            return v;
        }
        public void bind(Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public void bind(String n, Object o) throws NamingException { store.put(n, o); }
        public void rebind(Name n, Object o) { store.put(n.toString(), o); }
        public void rebind(String n, Object o) { store.put(n, o); }
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
        public NamingEnumeration<NameClassPair> list(Name n) { return dRefableEmptyNc(); }
        public NamingEnumeration<NameClassPair> list(String n) { return dRefableEmptyNc(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return dRefableEmptyB(); }
        public NamingEnumeration<Binding> listBindings(String n) { return dRefableEmptyB(); }
        public void destroySubcontext(Name n) { }
        public void destroySubcontext(String n) { }
        public Context createSubcontext(Name n) { return new DeepRefContext(); }
        public Context createSubcontext(String n) { return new DeepRefContext(); }
        public Object lookupLink(Name n) throws NamingException { return lookup(n); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public NameParser getNameParser(Name n) { return dRefableParser; }
        public NameParser getNameParser(String n) { return dRefableParser; }
        public Name composeName(Name n, Name pfx) throws NamingException {
            Name r = (Name) pfx.clone();
            r.addAll(n);
            return r;
        }
        public String composeName(String n, String pfx) throws NamingException {
            return composeName(new CompositeName(n), new CompositeName(pfx)).toString();
        }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public java.util.Hashtable<?, ?> getEnvironment() { return new java.util.Hashtable<>(); }
        public void close() { }
        public String getNameInNamespace() { return ""; }
    }

    static NamingEnumeration<NameClassPair> dRefableEmptyNc() {
        return new NamingEnumeration<NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        };
    }

    static NamingEnumeration<Binding> dRefableEmptyB() {
        return new NamingEnumeration<Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public Binding next() { throw new java.util.NoSuchElementException(); }
            public Binding nextElement() { return next(); }
            public void close() { }
        };
    }

    static final NameParser dRefableParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

}
