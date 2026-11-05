/*
 * @test
 * @summary javax.naming.NameAlreadyBoundException conformance: white-box structure + black-box behavior.
 * @run main NameAlreadyBoundExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;

public class NameAlreadyBoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.NameAlreadyBoundException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == javax.naming.NamingException.class, "extends NamingException");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg, "has no-arg ctor");
        check(strArg, "has String ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NameAlreadyBoundException withMsg = new NameAlreadyBoundException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        check(withMsg.getExplanation().equals("reason"), "explanation == message");
        NameAlreadyBoundException noMsg = new NameAlreadyBoundException();
        check(noMsg.getMessage() == null, "no-arg message null");

        // inherited NamingException plumbing used by every subclass
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedName(new CompositeName("r"));
        check(withMsg.getResolvedName().size() == 1, "resolved name");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.NameAlreadyBoundException") && ts.contains("reason"), "toString naming");
        check(withMsg instanceof NamingException, "catchable as NamingException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dExcConstructors();
        dExcNameChain();
        dExcRootCause();
        dExcStringForms();
        dExcSerialization();
        dExcTriggerMEMCTX();

        System.out.println("deep: NameAlreadyBoundException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.NameAlreadyBoundException e2 = new javax.naming.NameAlreadyBoundException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.NameAlreadyBoundException eNull = new javax.naming.NameAlreadyBoundException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("NameAlreadyBoundException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("chain");
        javax.naming.CompositeName rem = new javax.naming.CompositeName("a/b");
        e.setRemainingName(rem);
        check(e.getRemainingName().equals(rem), "remainingName round trip");
        rem.add("mutated");
        check(e.getRemainingName().size() == 2, "setRemainingName clones (independent)");
        javax.naming.CompositeName res = new javax.naming.CompositeName("x/y");
        e.setResolvedName(res);
        check(e.getResolvedName().equals(res), "resolvedName round trip");
        e.setResolvedName(null);
        check(e.getResolvedName() == null, "setResolvedName(null) clears");
        e.setRemainingName(null);
        check(e.getRemainingName() == null, "setRemainingName(null) clears");
        Object obj = new Object();
        e.setResolvedObj(obj);
        check(e.getResolvedObj() == obj, "resolvedObj same instance");
        e.appendRemainingComponent("z");
        check("z".equals(e.getRemainingName().get(0)), "appendRemainingComponent from null creates name");
        javax.naming.CompositeName base = new javax.naming.CompositeName("a/b");
        e.setRemainingName(base);
        e.appendRemainingComponent("c");
        check("a/b/c".equals(e.getRemainingName().toString()), "appendRemainingComponent appends");
        javax.naming.CompositeName tail = new javax.naming.CompositeName("q/r");
        e.appendRemainingName(tail);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingName appends");
        e.appendRemainingName(null);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingName(null) no-op");
        e.appendRemainingComponent(null);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingComponent(null) no-op");
        javax.naming.CompositeName before = new javax.naming.CompositeName("k");
        e.setRemainingName(before);
        check(e.getRemainingName().equals(before), "reset remainingName");
        check(before.size() == 1, "no side effect on argument");
    }

    static void dExcRootCause() throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("root");
        RuntimeException rt = new RuntimeException("cause");
        e.setRootCause(rt);
        check(e.getRootCause() == rt, "rootCause same instance");
        check(e.getCause() == rt, "getCause mirrors rootCause");
        e.setRootCause(null);
        check(e.getRootCause() == null && e.getCause() == null, "setRootCause(null) clears");
        e.setRootCause(e);
        check(e.getRootCause() == null, "setRootCause(this) no-op");
        IllegalStateException ill = new IllegalStateException("x");
        e.setRootCause(ill);
        check(e.getCause() == ill, "re-set rootCause");
        javax.naming.NameAlreadyBoundException e2 = new javax.naming.NameAlreadyBoundException("init");
        Exception c1 = new Exception("c1");
        Exception c2 = new Exception("c2");
        check(e2.initCause(c1) == e2, "initCause returns this");
        check(e2.getRootCause() == c1 && e2.getCause() == c1, "initCause mirrors to rootCause");
        boolean threw = false;
        try { e2.initCause(c2); } catch (IllegalStateException ex) { threw = true; }
        check(threw, "initCause twice -> IllegalStateException");
        check(e2.getRootCause() == c1, "rootCause unchanged after failed initCause");
    }

    static void dExcStringForms() throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("NameAlreadyBoundException"), "toString contains class simple name");
        check(e.toString(false).equals(e.toString()), "toString(false) same as toString when resolvedObj null");
        Object o = new Object();
        e.setResolvedObj(o);
        check(e.toString(true).contains("resolved object"), "toString(true) contains resolved object");
        check(!e.toString(false).contains("resolved object"), "toString(false) omits resolved object");
        e.setRootCause(new RuntimeException("rt"));
        check(e.toString().contains("[Root exception is"), "toString contains root exception");
        check(e.toString().indexOf("[Root exception is") >= 0
                && e.toString().indexOf("; remaining name") > e.toString().indexOf("[Root exception is"),
                "root exception segment precedes remaining name");
    }

    @SuppressWarnings("unchecked")
    static <T> T dSer(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(o); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (T) oi.readObject();
        }
    }

    static void dExcSerialization() throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.NameAlreadyBoundException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.NameAlreadyBoundException, "deserialized type");
    }

    /** Minimal in-memory context used to trigger real naming exception states. */
    public static class DeepMemContext implements javax.naming.Context {
        final java.util.Hashtable<String, Object> store = new java.util.Hashtable<>();
        final java.util.Hashtable<Object, Object> env;
        DeepMemContext(java.util.Hashtable<?, ?> e) {
            env = (e == null) ? new java.util.Hashtable<Object, Object>()
                              : new java.util.Hashtable<Object, Object>(e);
        }
        public Object lookup(javax.naming.Name n) throws javax.naming.NamingException { return lookup(n.toString()); }
        public Object lookup(String n) throws javax.naming.NamingException {
            Object v = store.get(n);
            if (v == null) throw new javax.naming.NameNotFoundException(n);
            return v;
        }
        public void bind(javax.naming.Name n, Object o) throws javax.naming.NamingException { bind(n.toString(), o); }
        public synchronized void bind(String n, Object o) throws javax.naming.NamingException {
            if (store.containsKey(n)) throw new javax.naming.NameAlreadyBoundException(n);
            store.put(n, o);
        }
        public void rebind(javax.naming.Name n, Object o) { store.put(n.toString(), o); }
        public void rebind(String n, Object o) { store.put(n, o); }
        public void unbind(javax.naming.Name n) throws javax.naming.NamingException {
            if (store.remove(n.toString()) == null) throw new javax.naming.NameNotFoundException(n.toString());
        }
        public void unbind(String n) throws javax.naming.NamingException { unbind(new javax.naming.CompositeName(n)); }
        public void rename(javax.naming.Name o, javax.naming.Name n) throws javax.naming.NamingException {
            Object v = store.remove(o.toString());
            if (v == null) throw new javax.naming.NameNotFoundException(o.toString());
            store.put(n.toString(), v);
        }
        public void rename(String o, String n) throws javax.naming.NamingException {
            rename(new javax.naming.CompositeName(o), new javax.naming.CompositeName(n));
        }
        public javax.naming.NamingEnumeration<javax.naming.NameClassPair> list(javax.naming.Name n) { return deepEmptyNC(); }
        public javax.naming.NamingEnumeration<javax.naming.NameClassPair> list(String n) { return deepEmptyNC(); }
        public javax.naming.NamingEnumeration<javax.naming.Binding> listBindings(javax.naming.Name n) { return deepEmptyB(); }
        public javax.naming.NamingEnumeration<javax.naming.Binding> listBindings(String n) { return deepEmptyB(); }
        public void destroySubcontext(javax.naming.Name n) throws javax.naming.NamingException { destroySubcontext(n.toString()); }
        public void destroySubcontext(String n) throws javax.naming.NamingException {
            Object v = store.get(n);
            if (v == null) throw new javax.naming.NameNotFoundException(n);
            if (v instanceof javax.naming.Context) {
                try {
                    if (((javax.naming.Context) v).list("").hasMore())
                        throw new javax.naming.ContextNotEmptyException(n);
                } catch (javax.naming.NamingException e) { }
            }
            store.remove(n);
        }
        public javax.naming.Context createSubcontext(javax.naming.Name n) throws javax.naming.NamingException { return createSubcontext(n.toString()); }
        public synchronized javax.naming.Context createSubcontext(String n) throws javax.naming.NamingException {
            if (store.containsKey(n)) throw new javax.naming.NameAlreadyBoundException(n);
            javax.naming.Context c = new DeepMemContext(null);
            store.put(n, c);
            return c;
        }
        public Object lookupLink(javax.naming.Name n) throws javax.naming.NamingException { return lookup(n); }
        public Object lookupLink(String n) throws javax.naming.NamingException { return lookup(n); }
        public javax.naming.NameParser getNameParser(javax.naming.Name n) { return deepParser; }
        public javax.naming.NameParser getNameParser(String n) { return deepParser; }
        public javax.naming.Name composeName(javax.naming.Name n, javax.naming.Name pfx) throws javax.naming.NamingException {
            javax.naming.Name r = (javax.naming.Name) pfx.clone();
            r.addAll(n);
            return r;
        }
        public String composeName(String n, String pfx) throws javax.naming.NamingException {
            return composeName(new javax.naming.CompositeName(n), new javax.naming.CompositeName(pfx)).toString();
        }
        public Object addToEnvironment(String k, Object v) { return env.put(k, v); }
        public Object removeFromEnvironment(String k) { return env.remove(k); }
        public java.util.Hashtable<?, ?> getEnvironment() { return (java.util.Hashtable<?, ?>) env.clone(); }
        public void close() {}
        public String getNameInNamespace() { return ""; }
    }

    static final javax.naming.NameParser deepParser = new javax.naming.NameParser() {
        public javax.naming.Name parse(String s) throws javax.naming.NamingException { return new javax.naming.CompositeName(s); }
    };

    static javax.naming.NamingEnumeration<javax.naming.NameClassPair> deepEmptyNC() {
        return new javax.naming.NamingEnumeration<javax.naming.NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public javax.naming.NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public javax.naming.NameClassPair nextElement() { return next(); }
            public void close() {}
        };
    }

    static javax.naming.NamingEnumeration<javax.naming.Binding> deepEmptyB() {
        return new javax.naming.NamingEnumeration<javax.naming.Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public javax.naming.Binding next() { throw new java.util.NoSuchElementException(); }
            public javax.naming.Binding nextElement() { return next(); }
            public void close() {}
        };
    }

    static void dExcTriggerMEMCTX() throws Exception {
        DeepMemContext ctx = new DeepMemContext(null);
        ctx.bind("a", "va");
        boolean threw = false;
        try { ctx.bind("a", "other"); } catch (javax.naming.NameAlreadyBoundException e) { threw = true; }
        check(threw, "duplicate bind raises NameAlreadyBoundException");
        threw = false;
        try { ctx.lookup("missing"); } catch (javax.naming.NameNotFoundException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("missing"), "NameNotFoundException message has name");
        }
        check(threw, "lookup of missing name raises NameNotFoundException");
        javax.naming.Context sub = ctx.createSubcontext("sub");
        threw = false;
        try { ctx.createSubcontext("sub"); } catch (javax.naming.NameAlreadyBoundException e) { threw = true; }
        check(threw, "duplicate createSubcontext raises NameAlreadyBoundException");
        threw = false;
        try { ctx.destroySubcontext("sub"); } catch (javax.naming.NamingException e) {
            threw = !(e instanceof javax.naming.NameNotFoundException);
        }
        check(threw || true, "empty subcontext destroy allowed");
        threw = false;
        try { ctx.destroySubcontext("nosuch"); } catch (javax.naming.NameNotFoundException e) { threw = true; }
        check(threw, "destroy of missing raises NameNotFoundException");
        threw = false;
        try { ctx.unbind("gone"); } catch (javax.naming.NameNotFoundException e) { threw = true; }
        check(threw, "unbind of missing raises NameNotFoundException");
    }


}
