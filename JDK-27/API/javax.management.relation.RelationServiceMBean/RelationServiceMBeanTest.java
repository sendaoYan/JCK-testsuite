/*
 * @test
 * @summary javax.management.relation.RelationServiceMBean conformance: white-box structure + light behavior.
 * @run main RelationServiceMBeanTest
 */
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceMBean;

public class RelationServiceMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationServiceMBean");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationServiceMBean must be public");
        check(c.isInterface(), "RelationServiceMBean must be an interface");

        c.getMethod("isActive");
        c.getMethod("getPurgeFlag");
        c.getMethod("setPurgeFlag", boolean.class);
        c.getMethod("createRelationType", String.class, javax.management.relation.RoleInfo[].class);
        c.getMethod("addRelationType", javax.management.relation.RelationType.class);
        c.getMethod("getAllRelationTypeNames");
        c.getMethod("getRoleInfos", String.class);
        c.getMethod("getRoleInfo", String.class, String.class);
        c.getMethod("removeRelationType", String.class);
        c.getMethod("createRelation", String.class, String.class, javax.management.relation.RoleList.class);
        c.getMethod("isRelationMBean", String.class);
        c.getMethod("isRelation", javax.management.ObjectName.class);
        c.getMethod("hasRelation", String.class);
        c.getMethod("getAllRelationIds");
        c.getMethod("checkRoleReading", String.class, String.class);
        c.getMethod("checkRoleWriting", javax.management.relation.Role.class, String.class, Boolean.class);
        c.getMethod("getRole", String.class, String.class);
        c.getMethod("getRoles", String.class, String[].class);
        c.getMethod("getAllRoles", String.class);
        c.getMethod("getRoleCardinality", String.class, String.class);
        c.getMethod("setRole", String.class, javax.management.relation.Role.class);
        c.getMethod("setRoles", String.class, javax.management.relation.RoleList.class);
        c.getMethod("getReferencedMBeans", String.class);
        c.getMethod("getRelationTypeName", String.class);
        c.getMethod("findReferencingRelations", javax.management.ObjectName.class, String.class, String.class);
        c.getMethod("findAssociatedMBeans", javax.management.ObjectName.class, String.class, String.class);
        c.getMethod("findRelationsOfType", String.class);
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(RelationServiceMBean.class.isAssignableFrom(RelationService.class),
              "RelationService implements RelationServiceMBean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
