/*
 * @test
 * @summary java.lang.reflect.ReflectPermission conformance: name/actions/equals.
 *          Deepened batch: two-arg constructor ignores actions completely (getActions is
 *          always the empty string, even for null actions), BasicPermission implies
 *          lattice (self, wildcard *, hierarchical packages, sibling, foreign permission
 *          type), null name NPE / empty name IAE with canonical messages (same wording on
 *          JDK-8 and JDK-27), mid-name wildcards are accepted as plain names, final class
 *          census, Guard contract, PermissionCollection behavior, and a serialization
 *          round-trip that keeps name and equality.
 * @run main ReflectPermissionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;
import java.security.BasicPermission;
import java.security.Permission;

public class ReflectPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                String m = t.getMessage();
                check(m != null && m.contains(fragment),
                        msg + ": message should contain \"" + fragment + "\" but was " + m);
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Object serRoundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    static void testReflectPermission() {
        ReflectPermission p = new ReflectPermission("suppressAccessChecks");
        check("suppressAccessChecks".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ReflectPermission("suppressAccessChecks")), "equals");
        check(p.hashCode() == new ReflectPermission("suppressAccessChecks").hashCode(), "hashCode");
        ReflectPermission p2 = new ReflectPermission("newProxyInPackage.*");
        check("newProxyInPackage.*".equals(p2.getName()), "wildcard name");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testHierarchyCensus() {
        check(BasicPermission.class.isAssignableFrom(ReflectPermission.class),
                "extends BasicPermission");
        check(Permission.class.isAssignableFrom(ReflectPermission.class),
                "extends Permission");
        check(java.security.Guard.class.isAssignableFrom(ReflectPermission.class),
                "usable as Guard");
        check(java.io.Serializable.class.isAssignableFrom(ReflectPermission.class),
                "Serializable");
        check(Modifier.isFinal(ReflectPermission.class.getModifiers()),
                "class is final (no user subclasses)");
        check(ReflectPermission.class.getConstructors().length == 2,
                "two public constructors: (String) and (String,String)");
    }

    static void testActionsIgnored() {
        ReflectPermission one = new ReflectPermission("suppressAccessChecks");
        ReflectPermission two = new ReflectPermission("suppressAccessChecks", "read,write");
        ReflectPermission nullActions = new ReflectPermission("suppressAccessChecks", null);
        check("".equals(two.getActions()), "two-arg actions normalized to empty");
        check("".equals(nullActions.getActions()), "null actions also empty");
        check(one.equals(two), "actions ignored by equals");
        check(one.equals(nullActions), "null actions equal to one-arg");
        check(one.hashCode() == two.hashCode(), "hashCode ignores actions");
        check(one.implies(two) && two.implies(one), "implies ignores actions");
    }

    static void testImpliesLattice() {
        Permission suppress = new ReflectPermission("suppressAccessChecks");
        check(suppress.implies(suppress), "self implies");
        check(new ReflectPermission("*").implies(suppress), "wildcard * implies specific");
        check(!suppress.implies(new ReflectPermission("*")),
                "specific does not imply *");
        check(!suppress.implies(new ReflectPermission("newProxyInPackage.java.lang")),
                "different name not implied");
        Permission pkg = new ReflectPermission("newProxyInPackage.java.lang");
        check(new ReflectPermission("newProxyInPackage.*").implies(pkg),
                "package wildcard implies");
        check(pkg.implies(pkg), "self implies hierarchical");
        check(!pkg.implies(new ReflectPermission("newProxyInPackage.java.util")),
                "sibling not implied");
        check(!new ReflectPermission("newProxyInPackage.java.lang.reflect")
                .implies(new ReflectPermission("newProxyInPackage.java.lang")),
                "deeper package does not imply shallower");
        // a ReflectPermission never implies a different permission class
        check(!suppress.implies(new java.lang.RuntimePermission("accessDeclaredMembers")),
                "foreign permission type never implied");
    }

    static void testNameValidation() {
        checkThrowsMsg(NullPointerException.class,
                () -> new ReflectPermission(null),
                "name can't be null", "null name NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> new ReflectPermission(""),
                "name can't be empty", "empty name IAE");
        checkThrowsMsg(NullPointerException.class,
                () -> new ReflectPermission(null, "read"),
                "name can't be null", "two-arg null name NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> new ReflectPermission("", "read"),
                "name can't be empty", "two-arg empty name IAE");
        // mid-name wildcards are accepted as ordinary names on both JDK-8 and JDK-27
        check(new ReflectPermission("a*b").getName().equals("a*b"),
                "mid-name * accepted as plain name");
        check(new ReflectPermission("a?b").getName().equals("a?b"),
                "mid-name ? accepted as plain name");
        check(new ReflectPermission("*").implies(new ReflectPermission("a*b")),
                "wildcard * still implies plain mid-star name");
        check(new ReflectPermission("a*b").equals(new ReflectPermission("a*b")),
                "mid-star name equality");
    }

    static void testEqualityForeignTypes() {
        ReflectPermission p = new ReflectPermission("suppressAccessChecks");
        check(!p.equals(null), "not equal to null");
        check(!p.equals("suppressAccessChecks"), "not equal to foreign type");
        check(!p.equals(new java.lang.RuntimePermission("suppressAccessChecks")),
                "not equal to same-named foreign permission");
        check(!new java.lang.RuntimePermission("suppressAccessChecks").implies(p),
                "foreign permission does not imply this one");
        check(p.toString().contains("suppressAccessChecks"), "toString contains name");
    }

    static void testPermissionCollection() {
        java.security.PermissionCollection pc =
                new ReflectPermission("suppressAccessChecks").newPermissionCollection();
        check(pc != null, "newPermissionCollection returns a live collection");
        pc.add(new ReflectPermission("newProxyInPackage.java.util"));
        check(pc.implies(new ReflectPermission("newProxyInPackage.java.util")),
                "collection implies added");
        check(!pc.implies(new ReflectPermission("suppressAccessChecks")),
                "collection does not imply unadded");
        pc.add(new ReflectPermission("*"));
        check(pc.implies(new ReflectPermission("suppressAccessChecks")),
                "collection wildcard covers everything");
        pc.add(new ReflectPermission("newProxyInPackage.*"));
        check(pc.implies(new ReflectPermission("newProxyInPackage.java.lang")),
                "collection hierarchical wildcard covers");
        check(pc.elements().hasMoreElements(), "collection enumerates");
    }

    static void testSerialization() throws Exception {
        ReflectPermission orig = new ReflectPermission("suppressAccessChecks", "ignored");
        Object back = serRoundTrip(orig);
        check(back instanceof ReflectPermission, "round-trip keeps exact type");
        ReflectPermission res = (ReflectPermission) back;
        check("suppressAccessChecks".equals(res.getName()), "round-trip keeps name");
        check("".equals(res.getActions()), "round-trip keeps empty actions");
        check(res.equals(orig), "round-trip equals original");
        check(res.hashCode() == orig.hashCode(), "round-trip hashCode stable");
        check(res.implies(orig) && orig.implies(res), "round-trip implies symmetric");
    }

    public static void main(String[] args) throws Exception {
        testReflectPermission();
        testHierarchyCensus();
        testActionsIgnored();
        testImpliesLattice();
        testNameValidation();
        testEqualityForeignTypes();
        testPermissionCollection();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
