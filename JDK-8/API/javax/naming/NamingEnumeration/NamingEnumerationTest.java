/*
 * @test
 * @summary javax.naming.NamingEnumeration conformance: interface hierarchy and members.
 * @run main NamingEnumerationTest
 */



import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.NoSuchElementException;
public class NamingEnumerationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.NamingEnumeration");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.Enumeration");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("close"), "has method close");
        check(names.contains("hasMore"), "has method hasMore");
        check(names.contains("hasMoreElements"), "has method hasMoreElements");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNeSequence();
        dNeCloseSemantics();
        dNeFromContext();
        System.out.println("deep: NamingEnumeration contracts OK");
    }

    static final class DeepListEnumeration implements NamingEnumeration<String> {
        final java.util.List<String> items;
        int cursor;
        boolean closed;
        DeepListEnumeration(java.util.List<String> items) { this.items = new java.util.ArrayList<>(items); }
        public boolean hasMore() { return !closed && cursor < items.size(); }
        public boolean hasMoreElements() { return hasMore(); }
        public String next() {
            if (closed) throw new NoSuchElementException("enumeration closed");
            if (cursor >= items.size()) throw new NoSuchElementException("no more elements");
            return items.get(cursor++);
        }
        public String nextElement() { return next(); }
        public void close() { closed = true; }
    }

    static void dNeSequence() throws Exception {
        NamingEnumeration<String> e = new DeepListEnumeration(java.util.Arrays.asList("a", "b", "c"));
        check(e.hasMore(), "hasMore before start");
        java.util.List<String> seen = new java.util.ArrayList<>();
        while (e.hasMore()) seen.add(e.next());
        check(seen.size() == 3 && "a".equals(seen.get(0)) && "c".equals(seen.get(2)), "iteration order");
        check(!e.hasMore(), "hasMore false at end");
        boolean nsee = false;
        try { e.next(); } catch (NoSuchElementException ex) { nsee = true; }
        check(nsee, "next after end -> NoSuchElementException");
        nsee = false;
        try { e.nextElement(); } catch (NoSuchElementException ex) { nsee = true; }
        check(nsee, "nextElement after end -> NoSuchElementException");
    }

    static void dNeCloseSemantics() throws Exception {
        DeepListEnumeration e = new DeepListEnumeration(java.util.Arrays.asList("a", "b"));
        check("a".equals(e.next()), "first element");
        e.close();
        e.close(); // idempotent close must not throw
        check(!e.hasMore(), "closed enumeration reports no more");
        boolean nsee = false;
        try { e.next(); } catch (NoSuchElementException ex) { nsee = true; }
        check(nsee, "closed enumeration next -> NoSuchElementException");
        NamingEnumeration<String> empty = new DeepListEnumeration(java.util.Collections.<String>emptyList());
        check(!empty.hasMore(), "empty hasMore false");
        boolean thrown = false;
        try { empty.next(); } catch (NoSuchElementException ex) { thrown = true; }
        check(thrown, "empty next -> NoSuchElementException");
    }

    static void dNeFromContext() throws Exception {
        DeepListContext ctx = new DeepListContext();
        ctx.bind("alpha", "va");
        ctx.bind("beta", "vb");
        NamingEnumeration<NameClassPair> nc = ctx.list("");
        java.util.List<String> names = new java.util.ArrayList<>();
        while (nc.hasMore()) names.add(nc.next().getName());
        java.util.Collections.sort(names);
        check(names.size() == 2 && "alpha".equals(names.get(0)) && "beta".equals(names.get(1)),
                "list enumeration yields bound names");
        NamingEnumeration<Binding> nb = ctx.listBindings("");
        Binding first = nb.next();
        check(first.getObject() instanceof String, "listBindings yields objects");
        check("va".equals(first.getObject()) || "vb".equals(first.getObject()), "object round trip");
        nb.close();
        check(!nb.hasMore(), "closed context enumeration stops");
    }

    static final class DeepListContext implements Context {
        final java.util.Map<String, Object> store = new java.util.TreeMap<>();
        public Object lookup(Name n) throws NamingException { return lookup(n.toString()); }
        public Object lookup(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            return v;
        }
        public void bind(Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public void bind(String n, Object o) throws NamingException {
            if (store.containsKey(n)) throw new NameAlreadyBoundException(n);
            store.put(n, o);
        }
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
        public NamingEnumeration<NameClassPair> list(Name n) { return list(""); }
        public NamingEnumeration<NameClassPair> list(String n) {
            final java.util.Iterator<java.util.Map.Entry<String, Object>> it = store.entrySet().iterator();
            return new NamingEnumeration<NameClassPair>() {
                boolean closed;
                public boolean hasMore() { return !closed && it.hasNext(); }
                public boolean hasMoreElements() { return hasMore(); }
                public NameClassPair next() {
                    java.util.Map.Entry<String, Object> en = it.next();
                    return new NameClassPair(en.getKey(), en.getValue().getClass().getName());
                }
                public NameClassPair nextElement() { return next(); }
                public void close() { closed = true; }
            };
        }
        public NamingEnumeration<Binding> listBindings(Name n) { return listBindings(""); }
        public NamingEnumeration<Binding> listBindings(String n) {
            final java.util.Iterator<java.util.Map.Entry<String, Object>> it = store.entrySet().iterator();
            return new NamingEnumeration<Binding>() {
                boolean closed;
                public boolean hasMore() { return !closed && it.hasNext(); }
                public boolean hasMoreElements() { return hasMore(); }
                public Binding next() {
                    java.util.Map.Entry<String, Object> en = it.next();
                    return new Binding(en.getKey(), en.getValue());
                }
                public Binding nextElement() { return next(); }
                public void close() { closed = true; }
            };
        }
        public void destroySubcontext(Name n) throws NamingException { }
        public void destroySubcontext(String n) throws NamingException { }
        public Context createSubcontext(Name n) { return new DeepListContext(); }
        public Context createSubcontext(String n) { return new DeepListContext(); }
        public Object lookupLink(Name n) throws NamingException { return lookup(n); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public NameParser getNameParser(Name n) { return dNeParser; }
        public NameParser getNameParser(String n) { return dNeParser; }
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

    static final NameParser dNeParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

}
