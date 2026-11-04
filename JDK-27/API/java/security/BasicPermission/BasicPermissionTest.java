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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
