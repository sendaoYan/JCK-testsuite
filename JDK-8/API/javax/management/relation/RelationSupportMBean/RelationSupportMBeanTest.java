/*
 * @test
 * @summary javax.management.relation.RelationSupportMBean conformance: interface reflection.
 * @run main RelationSupportMBeanTest
 */

public class RelationSupportMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupportMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.relation.Relation").isAssignableFrom(c), "implements/extends javax.management.relation.Relation");
        check(hasMethod(c, "isInRelationService"), "declares isInRelationService");
        check(hasMethod(c, "setRelationServiceManagementFlag"), "declares setRelationServiceManagementFlag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
