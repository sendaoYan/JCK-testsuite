/*
 * @test
 * @summary javax.naming.spi.ResolveResult conformance: white-box structure + black-box behavior.
 * @run main ResolveResultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.Serializable;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.spi.ResolveResult;

public class ResolveResultTest {
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
        Class<?> c = Class.forName("javax.naming.spi.ResolveResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean serializable = false;
        for (Class<?> itf : c.getInterfaces()) if (itf == Serializable.class) serializable = true;
        check(serializable, "implements java.io.Serializable");
        check(hasMethod(c, "getRemainingName"), "getRemainingName");
        check(hasMethod(c, "getResolvedObj"), "getResolvedObj");
        check(hasMethod(c, "setRemainingName"), "setRemainingName");
        check(hasMethod(c, "appendRemainingName"), "appendRemainingName");
        check(hasMethod(c, "appendRemainingComponent"), "appendRemainingComponent");
        check(hasMethod(c, "setResolvedObj"), "setResolvedObj");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor (Object, String) parses the remaining name as a CompositeName.
        ResolveResult r = new ResolveResult("obj", "a/b");
        check("obj".equals(r.getResolvedObj()), "string ctor resolved object");
        Name remaining = r.getRemainingName();
        check(remaining != null, "string ctor remaining name non-null");
        check(remaining instanceof CompositeName, "string ctor remaining is CompositeName");
        check(remaining.size() == 2, "CompositeName has two components");
        check("a".equals(remaining.get(0)), "first component");
        check("b".equals(remaining.get(1)), "second component");
        check("a/b".equals(remaining.toString()), "remaining toString");

        // Constructor (Object, Name) clones the supplied name.
        CompositeName supplied = new CompositeName("x/y");
        ResolveResult r2 = new ResolveResult("robj", supplied);
        check("robj".equals(r2.getResolvedObj()), "name ctor resolved object");
        check(r2.getRemainingName() != supplied, "name ctor clones the name");
        check("x/y".equals(r2.getRemainingName().toString()), "cloned name content");
        supplied.add("z"); // mutate original; result must be unaffected
        check("x/y".equals(r2.getRemainingName().toString()), "clone isolates from original mutation");

        // setRemainingName replaces (and clones) the remaining name; null clears it.
        r2.setRemainingName(new CompositeName("p"));
        check("p".equals(r2.getRemainingName().toString()), "setRemainingName replaces");
        r2.setRemainingName(null);
        check(r2.getRemainingName() == null, "setRemainingName(null) clears");

        // appendRemainingName adds components to the end.
        ResolveResult r3 = new ResolveResult("o", "a");
        r3.appendRemainingName(new CompositeName("b/c"));
        check("a/b/c".equals(r3.getRemainingName().toString()), "appendRemainingName appends");

        // appendRemainingComponent adds a single component.
        r3.appendRemainingComponent("d");
        check("a/b/c/d".equals(r3.getRemainingName().toString()), "appendRemainingComponent appends");

        // appendRemainingComponent(null) is a no-op.
        r3.appendRemainingComponent(null);
        check("a/b/c/d".equals(r3.getRemainingName().toString()), "appendRemainingComponent(null) no-op");

        // setResolvedObj replaces the resolved object.
        r3.setResolvedObj("changed");
        check("changed".equals(r3.getResolvedObj()), "setResolvedObj replaces");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
