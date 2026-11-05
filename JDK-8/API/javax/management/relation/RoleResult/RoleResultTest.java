/*
 * @test
 * @summary javax.management.relation.RoleResult conformance: roles + unresolved.
 * @run main RoleResultTest
 */

import javax.management.relation.*;
public class RoleResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleResult rr = new RoleResult(new RoleList(), new RoleUnresolvedList());
        check(rr.getRoles() != null, "roles");
        check(rr.getRolesUnresolved() != null, "unresolved");
        rr.setRoles(new RoleList());
        rr.setRolesUnresolved(new RoleUnresolvedList());
        check(rr.getRoles() != null && rr.getRolesUnresolved() != null, "setters");
        testNullCtorShapes();
        testCtorDeepCopy();
        testSettersAndNullClearing();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch j): full contract coverage ----

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

    static Role roleOf(String name, String onSpec) {
        java.util.List<javax.management.ObjectName> v = new java.util.ArrayList<javax.management.ObjectName>();
        try { v.add(new javax.management.ObjectName(onSpec)); } catch (Exception e) { throw new RuntimeException(e); }
        return new Role(name, v);
    }

    static RoleUnresolved unresOf(String name, int problemType) {
        return new RoleUnresolved(name, null, problemType);
    }

    // null arguments are legal everywhere in RoleResult: fields simply stay null
    static void testNullCtorShapes() {
        RoleResult both = new RoleResult(null, null);
        check(both.getRoles() == null, "ctor(null, null): roles null");
        check(both.getRolesUnresolved() == null, "ctor(null, null): unresolved null");
        RoleResult leftOnly = new RoleResult(new RoleList(), null);
        check(leftOnly.getRoles() != null && leftOnly.getRoles().isEmpty(), "ctor(roles, null): roles non-null empty");
        check(leftOnly.getRolesUnresolved() == null, "ctor(roles, null): unresolved null");
        RoleResult rightOnly = new RoleResult(null, new RoleUnresolvedList());
        check(rightOnly.getRoles() == null, "ctor(null, unresolved): roles null");
        check(rightOnly.getRolesUnresolved() != null && rightOnly.getRolesUnresolved().isEmpty(),
              "ctor(null, unresolved): unresolved non-null empty");
    }

    // the constructor deep-copies: new list instances and per-element clones
    static void testCtorDeepCopy() {
        Role original = roleOf("deep", "d:t=a");
        RoleList src = new RoleList();
        src.add(original);
        RoleResult rr = new RoleResult(src, null);
        check(rr.getRoles().size() == 1, "roles content copied");
        check(rr.getRoles() != src, "RoleResult builds its own RoleList");
        check(rr.getRoles().get(0) != original, "elements are per-element clones, not shared references");
        original.setRoleName("mutated");
        check(((Role) rr.getRoles().get(0)).getRoleName().equals("deep"),
              "element-level deep copy is immune to later mutation of the source role");
        src.clear();
        check(rr.getRoles().size() == 1, "list-level deep copy is immune to source list mutation");

        RoleUnresolved origUnres = unresOf("u", RoleStatus.ROLE_NOT_READABLE);
        RoleUnresolvedList srcU = new RoleUnresolvedList();
        srcU.add(origUnres);
        RoleResult rr2 = new RoleResult(null, srcU);
        check(rr2.getRolesUnresolved().size() == 1, "unresolved content copied");
        check(rr2.getRolesUnresolved() != srcU, "RoleResult builds its own RoleUnresolvedList");
        check(rr2.getRolesUnresolved().get(0) != origUnres, "unresolved elements are per-element clones");
        origUnres.setRoleName("mutated");
        check(((RoleUnresolved) rr2.getRolesUnresolved().get(0)).getRoleName().equals("u"),
              "unresolved deep copy immune to later mutation");
    }

    // setters replace references (deep-copying), null clears; getRoles/getRolesUnresolved combination
    static void testSettersAndNullClearing() {
        RoleResult rr = new RoleResult(new RoleList(), new RoleUnresolvedList());
        RoleList first = new RoleList();
        first.add(roleOf("a", "d:t=a"));
        rr.setRoles(first);
        check(rr.getRoles().size() == 1 && ((Role) rr.getRoles().get(0)).getRoleName().equals("a"),
              "setRoles installs content");
        RoleList second = new RoleList();
        second.add(roleOf("b", "d:t=b"));
        second.add(roleOf("c", "d:t=c"));
        rr.setRoles(second);
        check(rr.getRoles().size() == 2, "setRoles replaces the previous list");
        first.add(roleOf("late", "d:t=l"));
        check(rr.getRoles().size() == 2, "setRoles defensively copies its argument");
        rr.setRoles(null);
        check(rr.getRoles() == null, "setRoles(null) clears to null");

        RoleUnresolvedList u1 = new RoleUnresolvedList();
        u1.add(unresOf("u1", 1));
        rr.setRolesUnresolved(u1);
        check(rr.getRolesUnresolved().size() == 1, "setRolesUnresolved installs content");
        rr.setRolesUnresolved(null);
        check(rr.getRolesUnresolved() == null, "setRolesUnresolved(null) clears to null");

        // combined counts: 3 resolved roles + 2 unresolved roles
        RoleList three = new RoleList();
        three.add(roleOf("r1", "d:t=1"));
        three.add(roleOf("r2", "d:t=2"));
        three.add(roleOf("r3", "d:t=3"));
        RoleUnresolvedList two = new RoleUnresolvedList();
        two.add(unresOf("x1", 4));
        two.add(unresOf("x2", 5));
        RoleResult combo = new RoleResult(three, two);
        check(combo.getRoles().size() + combo.getRolesUnresolved().size() == 5,
              "getRoles().size() + getRolesUnresolved().size() combination");
    }

    // serialization round-trip + locked serialVersionUID + white-box contract
    static void testSerialization() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(RoleResult.class), "RoleResult implements Serializable");
        RoleList roles = new RoleList();
        roles.add(roleOf("s1", "d:t=1"));
        RoleUnresolvedList unres = new RoleUnresolvedList();
        unres.add(unresOf("u1", RoleStatus.NO_ROLE_WITH_NAME));
        RoleResult orig = new RoleResult(roles, unres);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        RoleResult back = (RoleResult) ois.readObject();
        ois.close();
        check(back.getRoles().size() == 1 && ((Role) back.getRoles().get(0)).getRoleName().equals("s1"),
              "serial roles round-trip");
        check(back.getRolesUnresolved().size() == 1
           && ((RoleUnresolved) back.getRolesUnresolved().get(0)).getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
              "serial unresolved round-trip");
        check(java.io.ObjectStreamClass.lookup(RoleResult.class).getSerialVersionUID()
              == -6304063118040985512L, "RoleResult serialVersionUID is -6304063118040985512L");
    }
}
