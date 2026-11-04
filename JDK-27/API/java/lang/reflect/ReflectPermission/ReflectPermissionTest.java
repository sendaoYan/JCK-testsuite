/*
 * @test
 * @summary java.lang.reflect.ReflectPermission conformance: white-box structure + black-box behavior.
 *          Deepened batch: canonical NPE/IAE message fragments, null-actions two-arg
 *          constructor, mid-name wildcards as plain names, final class census + Guard,
 *          foreign permission types, and a serialization round-trip.
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

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        ReflectPermission p = new ReflectPermission("suppressAccessChecks");
        check("suppressAccessChecks".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ReflectPermission("suppressAccessChecks")), "equals");
        check(p.hashCode() == new ReflectPermission("suppressAccessChecks").hashCode(), "hashCode");
        ReflectPermission p2 = new ReflectPermission("newProxyInPackage.*");
        check("newProxyInPackage.*".equals(p2.getName()), "wildcard name");
        check(BasicPermission.class.isAssignableFrom(ReflectPermission.class),
              "extends BasicPermission");
        check(java.security.Permission.class.isAssignableFrom(ReflectPermission.class),
              "extends Permission");
        check(!ReflectPermissionTest.class.getName().isEmpty(), "loaded");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. BasicPermission implies lattice for known reflect targets
        Permission suppress = new ReflectPermission("suppressAccessChecks");
        check(suppress.implies(suppress), "self implies");
        check(!suppress.implies(new ReflectPermission("newProxyInPackage.java.lang")),
              "different name not implied");
        check(new ReflectPermission("*").implies(suppress), "wildcard * implies specific");
        check(new ReflectPermission("suppressAccessChecks").implies(new ReflectPermission("*"))
              == false, "specific does not imply *");

        // 2. hierarchical names
        Permission pkg = new ReflectPermission("newProxyInPackage.java.lang");
        check(new ReflectPermission("newProxyInPackage.*").implies(pkg), "package wildcard implies");
        check(!pkg.implies(new ReflectPermission("newProxyInPackage.java.util")),
              "sibling not implied");
        check(pkg.implies(pkg), "self implies hierarchical");

        // 3. two-arg constructor ignores actions in comparisons
        ReflectPermission a1 = new ReflectPermission("suppressAccessChecks");
        ReflectPermission a2 = new ReflectPermission("suppressAccessChecks", "read");
        check(a1.equals(a2), "actions ignored for equals");
        check(a1.implies(a2) && a2.implies(a1), "actions ignored for implies");
        check("".equals(a1.getActions()), "one-arg actions empty");
        check("".equals(a2.getActions()), "BasicPermission normalizes actions to empty");

        // 4. equals/hashCode contract
        check(a1.equals(new ReflectPermission("suppressAccessChecks")), "equals same name");
        check(!a1.equals(new ReflectPermission("other")), "different names unequal");
        check(!a1.equals(null) && !a1.equals("suppressAccessChecks"), "foreign types unequal");
        check(a1.hashCode() == a2.hashCode(), "hashCode consistent with equals");

        // 5. name validation (null / empty rejected)
        try {
            new ReflectPermission(null);
            check(false, "null name must fail");
        } catch (NullPointerException e) {
            check(true, "NPE null name");
        }
        try {
            new ReflectPermission("");
            check(false, "empty name must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IAE empty name");
        }

        // 6. permission collection behavior
        java.security.PermissionCollection pc = a1.newPermissionCollection();
        pc.add(new ReflectPermission("newProxyInPackage.java.util"));
        check(pc.implies(new ReflectPermission("newProxyInPackage.java.util")),
              "collection implies added");
        check(!pc.implies(a1), "collection does not imply unadded");
        pc.add(new ReflectPermission("*"));
        check(pc.implies(a1), "collection wildcard covers");

        // 7. toString rendering
        check(a1.toString() != null && a1.toString().contains("suppressAccessChecks"),
              "toString contains name");
    }

    // ===================== deepened batch (2026-11-05) =====================

    static void testCanonicalNameValidationMessages() {
        checkThrowsMsg(NullPointerException.class,
                () -> new ReflectPermission(null), "name can't be null", "null name NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> new ReflectPermission(""), "name can't be empty", "empty name IAE");
        checkThrowsMsg(NullPointerException.class,
                () -> new ReflectPermission(null, "read"),
                "name can't be null", "two-arg null name NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> new ReflectPermission("", "read"),
                "name can't be empty", "two-arg empty name IAE");
        // mid-name wildcards are accepted as plain names (same behavior on JDK-8)
        check(new ReflectPermission("a*b").getName().equals("a*b"), "mid-name * accepted");
        check(new ReflectPermission("a?b").getName().equals("a?b"), "mid-name ? accepted");
        check(new ReflectPermission("*").implies(new ReflectPermission("a*b")),
              "wildcard * implies plain mid-star name");
        check(new ReflectPermission("a*b").equals(new ReflectPermission("a*b")),
              "mid-star name equality");
    }

    static void testActionsIgnoredDeep() {
        ReflectPermission one = new ReflectPermission("suppressAccessChecks");
        ReflectPermission nullActions = new ReflectPermission("suppressAccessChecks", null);
        check("".equals(nullActions.getActions()), "null actions also normalized to empty");
        check(one.equals(nullActions), "null actions equals one-arg");
        check(one.implies(nullActions) && nullActions.implies(one),
              "null actions implies symmetric");
    }

    static void testCensusAndForeign() {
        check(Modifier.isFinal(ReflectPermission.class.getModifiers()),
              "class is final (no user subclasses)");
        check(java.security.Guard.class.isAssignableFrom(ReflectPermission.class),
              "usable as Guard");
        check(ReflectPermission.class.getConstructors().length == 2,
              "two public constructors");
        // version difference: JDK 24+ deprecated this type for removal (JEP 486,
        // Security Manager permanently disabled); JDK-8 has no such annotation
        check(ReflectPermission.class.getAnnotation(Deprecated.class) != null,
              "deprecated for removal on this JDK while still fully functional");
        Permission p = new ReflectPermission("suppressAccessChecks");
        check(!p.implies(new java.lang.RuntimePermission("accessDeclaredMembers")),
              "never implies a foreign permission class");
        check(!new java.lang.RuntimePermission("suppressAccessChecks").implies(p),
              "foreign same-named permission does not imply this one");
        check(!p.equals(new java.lang.RuntimePermission("suppressAccessChecks")),
              "not equal to foreign permission with same name");
    }

    static void testSerializationRoundTrip() throws Exception {
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

    static void testPermissionCollectionEnumeration() {
        java.security.PermissionCollection pc =
                new ReflectPermission("x").newPermissionCollection();
        pc.add(new ReflectPermission("newProxyInPackage.java.util"));
        pc.add(new ReflectPermission("*"));
        java.util.Enumeration<Permission> en = pc.elements();
        int count = 0;
        while (en.hasMoreElements()) {
            check(en.nextElement() instanceof ReflectPermission,
                  "enumerated element is a ReflectPermission");
            count++;
        }
        check(count == 2, "collection enumerates both added permissions");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCanonicalNameValidationMessages();
        testActionsIgnoredDeep();
        testCensusAndForeign();
        testSerializationRoundTrip();
        testPermissionCollectionEnumeration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
