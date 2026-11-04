/*
 * @test
 * @summary javax.management.relation.RelationSupportMBean conformance: white-box structure + light behavior.
 * @run main RelationSupportMBeanTest
 */
import javax.management.relation.Relation;
import javax.management.relation.RelationSupport;
import javax.management.relation.RelationSupportMBean;

public class RelationSupportMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupportMBean");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationSupportMBean must be public");
        check(c.isInterface(), "RelationSupportMBean must be an interface");
        check(c.getSuperclass() == null, "interface must have no class superclass");
        // it extends Relation
        check(java.util.Arrays.asList(c.getInterfaces()).contains(Relation.class), "RelationSupportMBean must extend Relation");
        c.getMethod("isInRelationService");
        c.getMethod("setRelationServiceManagementFlag", Boolean.class);
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(RelationSupportMBean.class.isAssignableFrom(RelationSupport.class),
              "RelationSupport implements RelationSupportMBean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
