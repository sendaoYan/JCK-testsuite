/*
 * @test
 * @summary javax.management.relation.RelationServiceMBean conformance: interface reflection.
 * @run main RelationServiceMBeanTest
 */

public class RelationServiceMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationServiceMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addRelation"), "declares addRelation");
        check(hasMethod(c, "addRelationType"), "declares addRelationType");
        check(hasMethod(c, "checkRoleReading"), "declares checkRoleReading");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
