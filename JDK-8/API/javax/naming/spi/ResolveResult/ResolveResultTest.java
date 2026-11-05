/*
 * @test
 * @summary javax.naming.spi.ResolveResult conformance: resolved obj/remaining name.
 * @run main ResolveResultTest
 */



import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.spi.ResolveResult;
public class ResolveResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.spi.ResolveResult r = new javax.naming.spi.ResolveResult("obj", "a/b");
        check("obj".equals(r.getResolvedObj()), "getResolvedObj");
        check(r.getRemainingName() != null && r.getRemainingName().size() == 2, "getRemainingName");
        r.setResolvedObj("other");
        check("other".equals(r.getResolvedObj()), "setResolvedObj");
        r.appendRemainingComponent("c");
        check(r.getRemainingName().size() == 3, "appendRemainingComponent");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dRrConstructors();
        dRrMutation();
        dRrNulls();
        dRrSerialization();
        System.out.println("deep: ResolveResult contracts OK");
    }

    static void dRrConstructors() throws Exception {
        ResolveResult a = new ResolveResult("obj", "rest");
        check("obj".equals(a.getResolvedObj()), "resolved object recorded");
        check(new CompositeName("rest").equals(a.getRemainingName()),
                "String ctor -> single remaining component");
        check(a.getRemainingName().size() == 1, "one component");
        // empty remaining component is legal
        ResolveResult b = new ResolveResult("obj2", "");
        check(b.getRemainingName().isEmpty(), "empty remaining component OK");
        // Name ctor splits into components
        Name full = new CompositeName("a/b/c");
        ResolveResult c = new ResolveResult("obj3", full);
        check(c.getRemainingName().size() == 3, "Name ctor keeps components");
        check(c.getRemainingName().equals(full), "remaining equals source");
        check(c.getRemainingName() != full, "remaining is a copy, not same instance");
    }

    static void dRrMutation() throws Exception {
        // setRemainingName copies: later mutation of the source must not leak
        Name src = new CompositeName("a/b");
        ResolveResult r = new ResolveResult("obj", src);
        src.add("LEAK");
        check(r.getRemainingName().size() == 2, "setRemainingName copies source");
        // appendRemainingComponent appends one component
        r.appendRemainingComponent("c");
        check("a/b/c".equals(r.getRemainingName().toString()), "appendRemainingComponent");
        // appendRemainingName appends all components
        r.appendRemainingName(new CompositeName("d/e"));
        check("a/b/c/d/e".equals(r.getRemainingName().toString()), "appendRemainingName");
        // null appends are no-ops
        r.appendRemainingComponent(null);
        r.appendRemainingName(null);
        check("a/b/c/d/e".equals(r.getRemainingName().toString()), "null appends are no-ops");
        // setResolvedObj replaces
        r.setResolvedObj("other");
        check("other".equals(r.getResolvedObj()), "setResolvedObj");
        // setRemainingName(null) leaves remaining name null (documented quirk)
        r.setRemainingName(null);
        check(r.getRemainingName() == null, "setRemainingName(null) -> null field");
        // append onto a null remaining name clones the appended name
        r.appendRemainingName(new CompositeName("x/y"));
        check("x/y".equals(r.getRemainingName().toString()), "append onto null remaining name");
        r.appendRemainingComponent("z");
        check("x/y/z".equals(r.getRemainingName().toString()), "append component onto cloned name");
    }

    static void dRrNulls() {
        // robj itself is not null-checked at construction
        ResolveResult n = new ResolveResult(null, "a");
        check(n.getResolvedObj() == null, "null resolved object tolerated");
        // (String) null remaining component throws NPE (CompositeName(null))
        boolean npe = false;
        try { new ResolveResult("o", (String) null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "ResolveResult(obj, (String)null) -> NPE");
        // (Name) null is tolerated by the implementation (sets field null)
        ResolveResult m = new ResolveResult("o", (Name) null);
        check(m.getRemainingName() == null, "(Name)null tolerated, field null");
    }

    static void dRrSerialization() throws Exception {
        ResolveResult r = new ResolveResult("payload", new CompositeName("a/b"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(r);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ResolveResult back = (ResolveResult) ois.readObject();
            check("payload".equals(back.getResolvedObj()), "round-trip resolved obj");
            check(new CompositeName("a/b").equals(back.getRemainingName()), "round-trip remaining name");
        }
    }

}
