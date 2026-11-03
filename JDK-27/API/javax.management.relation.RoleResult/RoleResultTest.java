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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
