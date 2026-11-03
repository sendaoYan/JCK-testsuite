/*
 * @test
 * @summary javax.management.relation.RelationType conformance: white-box structure + light behavior.
 * @run main RelationTypeTest
 */
import java.io.Serializable;
import javax.management.relation.RelationType;
import javax.management.relation.RelationTypeSupport;
import javax.management.relation.RoleInfo;

public class RelationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationType must be public");
        check(c.isInterface(), "RelationType must be an interface");
        check(Serializable.class.isAssignableFrom(c), "RelationType must extend Serializable");
        c.getMethod("getRelationTypeName");
        c.getMethod("getRoleInfos");
        c.getMethod("getRoleInfo", String.class);
        check(c.getMethod("getRoleInfos").getReturnType() == java.util.List.class, "getRoleInfos returns List");
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(RelationType.class.isAssignableFrom(RelationTypeSupport.class),
              "RelationTypeSupport implements RelationType");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
