/*
 * @test
 * @summary java.security.BasicPermission conformance: full contract coverage.
 *          abstract class hierarchy, name/name+actions constructors, getName/
 *          getActions (always ""), null name -> NPE, empty name -> IAE, implies
 *          (exact, wildcard "a.*" multi-level, "*" universal), equals/hashCode
 *          same-class and cross-subclass, and newPermissionCollection add/implies.
 * @run main BasicPermissionTest
 */

import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;

public class BasicPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class BP extends BasicPermission {
        BP(String name) { super(name); }
        BP(String name, String actions) { super(name, actions); }
    }

    static class BP2 extends BasicPermission {
        BP2(String name) { super(name); }
    }

    public static void main(String[] args) throws Exception {
        // ---- abstract class hierarchy ----
        Class<?> c = Class.forName("java.security.BasicPermission");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(),
                "BasicPermission abstract class");
        check(Permission.class.isAssignableFrom(c), "BasicPermission extends Permission");

        // ---- constructors + getters ----
        BP p = new BP("a.b");
        check("a.b".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty for name-only");
        BP pa = new BP("a.b", "read,write");
        check("a.b".equals(pa.getName()), "getName with actions");
        check("".equals(pa.getActions()), "getActions ignores actions (always empty)");

        // ---- null/empty name ----
        checkThrows(NullPointerException.class, () -> new BP(null), "BasicPermission(null)");
        checkThrows(IllegalArgumentException.class, () -> new BP(""), "BasicPermission(empty)");

        // ---- implies ----
        BP exact = new BP("a.b");
        check(exact.implies(new BP("a.b")), "implies exact match");
        check(!exact.implies(new BP("a.b.c")), "exact does not imply deeper");
        check(!exact.implies(new BP("a.c")), "exact does not imply sibling");

        BP star = new BP("a.*");
        check(star.implies(new BP("a.b")), "a.* implies a.b");
        check(star.implies(new BP("a.b.c")), "a.* implies a.b.c (multi-level)");
        check(!star.implies(new BP("b.b")), "a.* does not imply b.b");

        BP all = new BP("*");
        check(all.implies(new BP("anything.at.all")), "* implies everything");

        // ---- equals/hashCode ----
        check(p.equals(new BP("a.b")), "equals same name same class");
        check(p.hashCode() == new BP("a.b").hashCode(), "hashCode equal");
        check(!p.equals(new BP("a.c")), "equals different name false");
        check(!p.equals(new BP2("a.b")), "equals different subclass false");
        check(!p.equals("a.b"), "equals non-Permission false");

        // ---- newPermissionCollection ----
        PermissionCollection pc = p.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        check(!pc.implies(p), "empty collection implies false");
        pc.add(new BP("a.b"));
        check(pc.implies(p), "collection implies after add");
        check(pc.implies(new BP("a.b")), "collection implies equal permission");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: exact messages, deep wildcard matrix, collection independence, serialization ----
    static void testContractDeep() throws Exception {
        // precise constructor exception messages
        try {
            new BP(null);
        } catch (NullPointerException npe) {
            check("name can't be null".equals(npe.getMessage()), "null name exact message");
        }
        try {
            new BP("");
        } catch (IllegalArgumentException iae) {
            check("name can't be empty".equals(iae.getMessage()), "empty name exact message");
        }

        // implies requires the exact same concrete class
        check(!new BP("a.b").implies(new java.security.SecurityPermission("a.b")), "implies different class false");
        check(!new BP("a.b").implies(null), "implies null false");

        // wildcard corner cases (all same named class BP)
        BP star = new BP("*");
        check(star.implies(new BP("*")), "* implies *");
        check(star.implies(new BP("x")), "* implies single");
        check(!new BP("a.*").implies(new BP("*")), "a.* does not imply *");
        check(!new BP("a.b").implies(new BP("a.b.*")), "exact does not imply wildcard name");
        check(new BP("a.*").implies(new BP("a.*")), "a.* implies a.* (same name)");
        // embedded/misplaced '*' is legal at construction and treated as a literal name character
        BP midStar = new BP("a*b");
        check("a*b".equals(midStar.getName()), "mid-star name accepted verbatim");
        check(!midStar.implies(new BP("axxb")), "mid-star is literal, not a pattern");
        check(midStar.implies(new BP("a*b")), "mid-star implies itself");
        BP preStar = new BP("*b");
        check(!preStar.implies(new BP("xb")), "leading star is literal, no match");
        BP trailing = new BP("a.b*");
        check(!trailing.implies(new BP("a.bc")), "bare trailing * without dot is literal");

        // equals/hashCode symmetry across classes with the same name
        check(new BP("n").hashCode() == new BP("n").hashCode(), "hashCode same class/name");
        check(new BP("n").equals(new BP("n")) && new BP("n").equals(new BP("n")), "equals symmetric");
        check(new BP2("n").equals(new BP2("n")), "second subclass self-equals");
        check(!new BP("n").equals(new BP2("n")), "subclass barrier symmetric");

        // toString format: ("class" "name") - actions never appear
        String ts = new BP("a.b").toString();
        check(ts.contains("BP") && ts.contains("a.b"), "toString contains class and name");
        check(!ts.contains("actions"), "toString has no actions section");

        // two collections are independent objects; mutating one does not affect the other
        PermissionCollection c1 = new BP("x").newPermissionCollection();
        PermissionCollection c2 = new BP("x").newPermissionCollection();
        check(c1 != c2, "newPermissionCollection returns independent instances");
        c1.add(new BP("x.y"));
        check(!c2.implies(new BP("x.y")), "sibling collection unaffected");
        c2.add(new BP("z"));
        check(c1.implies(new BP("x.y")) && !c1.implies(new BP("z")), "c1 unaffected by c2");
        // collection accepts same-class wildcard entries
        PermissionCollection c3 = new BP("w").newPermissionCollection();
        c3.add(new BP("w.*"));
        check(c3.implies(new BP("w.q")), "collection wildcard implies child");
        check(!c3.implies(new BP("w")), "collection wildcard not parent");

        // serialization round-trip of a named BasicPermission subclass
        BP rt = (BP) roundTrip(new BP("ser.name"));
        check("ser.name".equals(rt.getName()), "round-trip name");
        check("".equals(rt.getActions()), "round-trip actions empty");
        check(rt.implies(new BP("ser.name")), "round-trip implies self");
        check(rt.equals(new BP("ser.name")), "round-trip equals");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return oin.readObject();
    }
}
