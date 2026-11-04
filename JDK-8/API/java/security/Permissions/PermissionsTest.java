/*
 * @test
 * @summary java.security.Permissions conformance: full contract coverage.
 *          add/implies/elements across multiple permission classes, add(null) -> NPE,
 *          implies(null) -> NPE, deduplication of identical permissions, and
 *          PermissionCollection subtype relationship.
 * @run main PermissionsTest
 */

import java.security.AllPermission;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionsTest {
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
    }

    public static void main(String[] args) throws Exception {
        // ---- subtype ----
        check(PermissionCollection.class.isAssignableFrom(Permissions.class),
                "Permissions extends PermissionCollection");

        // ---- add + implies ----
        Permissions perms = new Permissions();
        check(!perms.implies(new BP("a.b")), "empty implies false");
        perms.add(new BP("a.b"));
        check(perms.implies(new BP("a.b")), "implies added BasicPermission");
        check(!perms.implies(new BP("a.c")), "does not imply other name");

        // multiple permission classes
        perms.add(new AllPermission());
        check(perms.implies(new BP("anything")), "AllPermission implies everything");

        // ---- elements enumeration ----
        Permissions p2 = new Permissions();
        p2.add(new BP("x"));
        p2.add(new java.security.SecurityPermission("y"));
        Set<String> names = new HashSet<>();
        for (Permission p : Collections.list(p2.elements())) names.add(p.getName());
        check(names.contains("x") && names.contains("y"), "elements enumerates all");

        // ---- deduplication ----
        Permissions p3 = new Permissions();
        p3.add(new BP("dup"));
        p3.add(new BP("dup"));
        check(Collections.list(p3.elements()).size() == 1, "identical permissions deduplicated");

        // ---- null handling ----
        final Permissions pn = new Permissions();
        checkThrows(NullPointerException.class, () -> pn.add((Permission) null), "add(null)");
        checkThrows(NullPointerException.class, () -> pn.implies(null), "implies(null)");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: class bucketing, AllPermission dominance, UP resolution, serialization ----
    static void testContractDeep() throws Exception {
        // bucketing is keyed on the concrete Permission CLASS: anonymous subclasses of the
        // same base do not match each other, the named class does
        Permissions byClass = new Permissions();
        byClass.add(new BP("np"));
        check(byClass.implies(new BP("np")), "same named class implies");
        byClass.add(new BasicPermission("anon") {});
        check(!byClass.implies(new BasicPermission("anon") {}), "distinct anonymous classes do not imply");
        check(!byClass.implies(new java.security.SecurityPermission("np")), "different class never implied");

        // wildcard inside one named class bucket
        Permissions wild = new Permissions();
        wild.add(new BP("svc.*"));
        check(wild.implies(new BP("svc.a")), "wildcard implies child");
        check(wild.implies(new BP("svc.a.b")), "wildcard implies deep child");
        check(!wild.implies(new BP("svc")), "wildcard does not imply parent name");
        check(!wild.implies(new BP("other")), "wildcard does not imply sibling");

        // AllPermission flips implies for every class, added before or after others
        Permissions dom = new Permissions();
        dom.add(new BP("first"));
        check(!dom.implies(new java.security.SecurityPermission("nothing")), "not implied before AllPermission");
        dom.add(new AllPermission());
        check(dom.implies(new java.security.SecurityPermission("nothing")), "AllPermission dominates all classes");
        check(dom.implies(new BP("later")), "AllPermission dominates BP class too");

        // read-only state machine: setReadOnly then add -> SecurityException with exact message
        Permissions ro = new Permissions();
        ro.add(new BP("ro"));
        ro.setReadOnly();
        check(ro.isReadOnly(), "readOnly set");
        checkThrows(SecurityException.class, () -> ro.add(new BP("x")),
                "add after setReadOnly");
        try {
            ro.add(new BP("y"));
        } catch (SecurityException se) {
            check("attempt to add a Permission to a readonly Permissions object".equals(se.getMessage()),
                    "readonly add exact message");
        }
        check(ro.implies(new BP("ro")), "readOnly keeps implies");

        // UnresolvedPermission bucket: never implies directly, but Permissions resolves it
        // against the queried permission's class+name
        Permissions ups = new Permissions();
        ups.add(new java.security.UnresolvedPermission(
                "java.security.SecurityPermission", "getPolicy", null, null));
        check(ups.implies(new java.security.SecurityPermission("getPolicy")),
                "Permissions resolves matching UnresolvedPermission");
        check(!ups.implies(new java.security.SecurityPermission("setLog")),
                "non-matching name not resolved");
        ups.add(new java.security.UnresolvedPermission("com.bogus.Missing", "n", null, null));
        check(!ups.implies(new java.security.SecurityPermission("n")), "unresolvable class never implies");

        // elements enumerates unresolved entries; UnresolvedPermission.getName() is the unresolved TYPE
        Set<String> en = new HashSet<>();
        for (Permission p : Collections.list(ups.elements())) en.add(p.getName());
        check(en.contains("java.security.SecurityPermission"), "elements shows UP type name");
        check(en.contains("com.bogus.Missing"), "elements shows second UP type name");

        // serialization round-trip: implies matrix fully preserved (incl. AllPermission bucket)
        Permissions ser = new Permissions();
        ser.add(new BP("s1"));
        ser.add(new BP("w.*"));
        ser.add(new java.security.SecurityPermission("sp1"));
        Permissions back = (Permissions) roundTrip(ser);
        check(back.implies(new BP("s1")), "ser keeps exact");
        check(back.implies(new BP("w.q")), "ser keeps wildcard");
        check(back.implies(new java.security.SecurityPermission("sp1")), "ser keeps other class");
        check(!back.implies(new java.security.SecurityPermission("sp2")), "ser keeps non-member");
        Permissions serAll = new Permissions();
        serAll.add(new AllPermission());
        check(((Permissions) roundTrip(serAll)).implies(new java.security.SecurityPermission("x")),
                "ser keeps AllPermission dominance");
        check(Collections.list(back.elements()).size() == 3, "ser keeps element count");
        check(!back.isReadOnly(), "default not readOnly");
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
