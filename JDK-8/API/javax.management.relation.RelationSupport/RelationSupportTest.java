/*
 * @test
 * @summary javax.management.relation.RelationSupport conformance: concrete class reflection.
 * @run main RelationSupportTest
 */

public class RelationSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.relation.RelationSupportMBean").isAssignableFrom(c), "implements/extends javax.management.relation.RelationSupportMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(hasMethod(c, "getAllRoles"), "declares getAllRoles");
        check(hasMethod(c, "getReferencedMBeans"), "declares getReferencedMBeans");
        check(hasMethod(c, "getRelationId"), "declares getRelationId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
