/*
 * @test
 * @summary javax.management.relation.RelationSupport conformance: white-box structure + black-box behavior.
 * @run main RelationSupportTest
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.Relation;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationSupport;
import javax.management.relation.RelationSupportMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleList;
import javax.management.relation.RoleNotFoundException;

public class RelationSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static Role role(String name, String... objNames) {
        List<ObjectName> v = new ArrayList<>();
        for (String s : objNames) v.add(on("d:type=" + s));
        return new Role(name, v);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupport");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationSupport must be public");
        check(!c.isInterface(), "RelationSupport must be a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "RelationSupport must be concrete");
        check(RelationSupportMBean.class.isAssignableFrom(c), "RelationSupport must implement RelationSupportMBean");
        check(Relation.class.isAssignableFrom(c), "RelationSupport must implement Relation");
        c.getConstructor(String.class, ObjectName.class, String.class, RoleList.class);
        c.getConstructor(String.class, ObjectName.class, MBeanServer.class, String.class, RoleList.class);
        c.getMethod("getRole", String.class); c.getMethod("getRoles", String[].class);
        c.getMethod("getAllRoles"); c.getMethod("retrieveAllRoles");
        c.getMethod("getRoleCardinality", String.class); c.getMethod("getReferencedMBeans");
        c.getMethod("getRelationTypeName"); c.getMethod("getRelationServiceName");
        c.getMethod("getRelationId"); c.getMethod("isInRelationService");
        c.getMethod("setRelationServiceManagementFlag", Boolean.class);
    }

    // ---- Part 2: black-box (behavioral, no real MBeanServer) ----
    static void testBlackBox() throws Exception {
        ObjectName relSvc = on("d:type=relationService");

        RoleList roles = new RoleList();
        roles.add(role("roleA", "m1", "m2"));
        roles.add(role("roleB", "m3"));

        RelationSupport rs = new RelationSupport("relId", relSvc, "myType", roles);
        check("relId".equals(rs.getRelationId()), "getRelationId");
        check("myType".equals(rs.getRelationTypeName()), "getRelationTypeName");
        check(relSvc.equals(rs.getRelationServiceName()), "getRelationServiceName");
        check(Boolean.FALSE.equals(rs.isInRelationService()), "isInRelationService default false");

        // retrieveAllRoles returns the role set (order unspecified)
        RoleList all = rs.retrieveAllRoles();
        check(all.size() == 2, "retrieveAllRoles size");
        List<String> names = new ArrayList<>();
        for (Object o : all) names.add(((Role) o).getRoleName());
        java.util.Collections.sort(names);
        check("roleA,roleB".equals(String.join(",", names)), "retrieveAllRoles role names");

        // getRoleCardinality (no MBeanServer needed for existing role)
        check(Integer.valueOf(2).equals(rs.getRoleCardinality("roleA")), "roleA cardinality 2");
        check(Integer.valueOf(1).equals(rs.getRoleCardinality("roleB")), "roleB cardinality 1");

        // getReferencedMBeans maps each ObjectName to the roles referencing it
        Map<ObjectName, List<String>> refs = rs.getReferencedMBeans();
        check(refs.size() == 3, "getReferencedMBeans size");
        check(refs.get(on("d:type=m1")).contains("roleA"), "m1 referenced by roleA");
        check(refs.get(on("d:type=m3")).contains("roleB"), "m3 referenced by roleB");

        // management flag setter/getter
        rs.setRelationServiceManagementFlag(Boolean.TRUE);
        check(Boolean.TRUE.equals(rs.isInRelationService()), "setRelationServiceManagementFlag true");

        // error cases that don't require a registered MBeanServer
        try { rs.getRole(null); check(false, "getRole(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "getRole null message"); }
        try { rs.getRole("nope"); check(false, "getRole(unknown) must throw"); }
        catch (RoleNotFoundException e) { check("nope does not exist in relation.".equals(e.getMessage()), "getRole unknown message: " + e.getMessage()); }
        try { rs.getRoles(null); check(false, "getRoles(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "getRoles null message"); }
        try { rs.getRoleCardinality(null); check(false, "getRoleCardinality(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "getRoleCardinality null message"); }
        try { rs.getRoleCardinality("nope"); check(false, "getRoleCardinality(unknown) must throw"); }
        catch (RoleNotFoundException e) { check("nope does not exist in relation.".equals(e.getMessage()), "cardinality unknown message"); }
        try { rs.setRelationServiceManagementFlag(null); check(false, "setFlag(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setFlag null message"); }
        try { rs.setRole(null); check(false, "setRole(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRole null message"); }
        try { rs.setRoles(null); check(false, "setRoles(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoles null message"); }

        // constructor validation
        try { new RelationSupport(null, relSvc, "t", null); check(false, "null relationId must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null id message"); }
        try { new RelationSupport("id", null, "t", null); check(false, "null service name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null service name message"); }
        try { new RelationSupport("id", relSvc, null, null); check(false, "null type name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null type name message"); }
        try { new RelationSupport("id", relSvc, (MBeanServer) null, "t", null); check(false, "null MBeanServer must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null MBeanServer message"); }
        try {
            RoleList dup = new RoleList();
            dup.add(role("r", "x"));
            dup.add(role("r", "y"));
            new RelationSupport("id", relSvc, "t", dup);
            check(false, "duplicate roles must throw");
        } catch (InvalidRoleValueException e) {
            check("Role name r used for two roles.".equals(e.getMessage()), "dup roles message: " + e.getMessage());
        }

        // getAllRoles on an empty relation (no MBeanServer access needed)
        RelationSupport empty = new RelationSupport("id", relSvc, "t", null);
        check(empty.getAllRoles() != null, "getAllRoles on empty relation returns a RoleResult");
        check(empty.getAllRoles().getRoles() != null && empty.getAllRoles().getRoles().isEmpty(), "empty getAllRoles roles");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
