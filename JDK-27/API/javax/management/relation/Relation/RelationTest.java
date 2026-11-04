/*
 * @test
 * @summary javax.management.relation.Relation conformance: white-box structure + light behavior.
 * @run main RelationTest
 */
import javax.management.relation.Relation;
import javax.management.relation.RelationSupport;

public class RelationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Relation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Relation must be public");
        check(c.isInterface(), "Relation must be an interface");

        c.getMethod("getRole", String.class);
        c.getMethod("getRoles", String[].class);
        c.getMethod("getRoleCardinality", String.class);
        c.getMethod("getAllRoles");
        c.getMethod("retrieveAllRoles");
        c.getMethod("setRole", javax.management.relation.Role.class);
        c.getMethod("setRoles", javax.management.relation.RoleList.class);
        c.getMethod("handleMBeanUnregistration", javax.management.ObjectName.class, String.class);
        c.getMethod("getReferencedMBeans");
        c.getMethod("getRelationTypeName");
        c.getMethod("getRelationServiceName");
        c.getMethod("getRelationId");

        check(c.getMethod("getRole", String.class).getReturnType() == java.util.List.class, "getRole returns List");
        check(c.getMethod("getAllRoles").getReturnType() == javax.management.relation.RoleResult.class, "getAllRoles returns RoleResult");
        check(c.getMethod("retrieveAllRoles").getReturnType() == javax.management.relation.RoleList.class, "retrieveAllRoles returns RoleList");
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(Relation.class.isAssignableFrom(RelationSupport.class),
              "RelationSupport implements Relation");
        // All methods are exercised indirectly through RelationSupport's own
        // behavioral tests; here we verify the interface is implementable by
        // its documented implementation class.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
