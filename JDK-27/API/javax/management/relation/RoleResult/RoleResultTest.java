/*
 * @test
 * @summary javax.management.relation.RoleResult conformance: white-box structure + black-box behavior.
 * @run main RoleResultTest
 */
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.relation.Role;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleUnresolved;
import javax.management.relation.RoleUnresolvedList;

public class RoleResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Role role(String name) {
        List<ObjectName> v = new ArrayList<>();
        try { v.add(new ObjectName("d:type=" + name)); } catch (Exception e) { throw new RuntimeException(e); }
        return new Role(name, v);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleResult must be public");
        check(!c.isInterface(), "RoleResult must be a class");
        check(java.io.Serializable.class.isAssignableFrom(c), "RoleResult must implement Serializable");
        c.getConstructor(RoleList.class, RoleUnresolvedList.class);
        c.getMethod("getRoles"); c.getMethod("getRolesUnresolved");
        c.getMethod("setRoles", RoleList.class); c.getMethod("setRolesUnresolved", RoleUnresolvedList.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RoleList roles = new RoleList();
        roles.add(role("ok1"));
        roles.add(role("ok2"));
        RoleUnresolvedList unresolved = new RoleUnresolvedList();
        unresolved.add(new RoleUnresolved("bad", null, RoleStatus.NO_ROLE_WITH_NAME));

        RoleResult rr = new RoleResult(roles, unresolved);
        check(rr.getRoles() != null && rr.getRoles().size() == 2, "getRoles after construct");
        check(rr.getRolesUnresolved() != null && rr.getRolesUnresolved().size() == 1, "getRolesUnresolved after construct");

        // setRoles/setRolesUnresolved defensively clone their inputs
        RoleList sourceRoles = new RoleList();
        sourceRoles.add(role("a"));
        rr.setRoles(sourceRoles);
        sourceRoles.add(role("b"));
        check(rr.getRoles().size() == 1, "setRoles clones its input");
        check("a".equals(((Role) rr.getRoles().get(0)).getRoleName()), "setRoles content");

        RoleUnresolvedList sourceUnres = new RoleUnresolvedList();
        sourceUnres.add(new RoleUnresolved("u", null, RoleStatus.ROLE_NOT_READABLE));
        rr.setRolesUnresolved(sourceUnres);
        sourceUnres.add(new RoleUnresolved("u2", null, RoleStatus.ROLE_NOT_READABLE));
        check(rr.getRolesUnresolved().size() == 1, "setRolesUnresolved clones its input");

        // null setters clear fields
        rr.setRoles(null);
        check(rr.getRoles() == null, "setRoles(null) clears");
        rr.setRolesUnresolved(null);
        check(rr.getRolesUnresolved() == null, "setRolesUnresolved(null) clears");

        // empty lists supported
        RoleResult empty = new RoleResult(new RoleList(), new RoleUnresolvedList());
        check(empty.getRoles() != null && empty.getRoles().isEmpty(), "empty roles");
        check(empty.getRolesUnresolved() != null && empty.getRolesUnresolved().isEmpty(), "empty unresolved");
    }

    // ---- deepened (batch j): null ctor shapes, element-level deep copy, serialization ----

    static void testNullCtorShapes() {
        RoleResult both = new RoleResult(null, null);
        check(both.getRoles() == null && both.getRolesUnresolved() == null, "ctor(null, null): both fields null");
        RoleResult leftOnly = new RoleResult(new RoleList(), null);
        check(leftOnly.getRoles() != null && leftOnly.getRolesUnresolved() == null, "ctor(roles, null)");
        RoleResult rightOnly = new RoleResult(null, new RoleUnresolvedList());
        check(rightOnly.getRoles() == null && rightOnly.getRolesUnresolved() != null, "ctor(null, unresolved)");
    }

    static void testElementLevelDeepCopy() {
        // elements are per-element clones: mutating a source Role afterwards is invisible
        Role original = role("deep");
        RoleList src = new RoleList();
        src.add(original);
        RoleResult rr = new RoleResult(src, null);
        check(rr.getRoles().get(0) != original, "elements are per-element clones, not shared references");
        original.setRoleName("mutated");
        check("deep".equals(((Role) rr.getRoles().get(0)).getRoleName()),
              "element-level deep copy is immune to later mutation of the source role");
        src.clear();
        check(rr.getRoles().size() == 1, "list-level deep copy is immune to source list mutation");
        // same for the unresolved side
        RoleUnresolved origUnres = new RoleUnresolved("u", null, RoleStatus.ROLE_NOT_READABLE);
        RoleUnresolvedList srcU = new RoleUnresolvedList();
        srcU.add(origUnres);
        RoleResult rr2 = new RoleResult(null, srcU);
        check(rr2.getRolesUnresolved().get(0) != origUnres, "unresolved elements are per-element clones");
        origUnres.setRoleName("mutated");
        check("u".equals(((RoleUnresolved) rr2.getRolesUnresolved().get(0)).getRoleName()),
              "unresolved deep copy immune to later mutation");
    }

    static void testSerialization() throws Exception {
        RoleList roles = new RoleList();
        roles.add(role("s1"));
        RoleUnresolvedList unres = new RoleUnresolvedList();
        unres.add(new RoleUnresolved("u1", null, RoleStatus.NO_ROLE_WITH_NAME));
        RoleResult orig = new RoleResult(roles, unres);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        RoleResult back = (RoleResult) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getRoles().size() == 1 && "s1".equals(((Role) back.getRoles().get(0)).getRoleName()),
              "serial roles round-trip");
        check(back.getRolesUnresolved().size() == 1
           && ((RoleUnresolved) back.getRolesUnresolved().get(0)).getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
              "serial unresolved round-trip");
        check(java.io.ObjectStreamClass.lookup(RoleResult.class).getSerialVersionUID()
              == -6304063118040985512L, "RoleResult serialVersionUID is -6304063118040985512L");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullCtorShapes();
        testElementLevelDeepCopy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
