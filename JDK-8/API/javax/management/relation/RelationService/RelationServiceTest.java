/*
 * @test
 * @summary javax.management.relation.RelationService conformance: concrete class reflection.
 * @run main RelationServiceTest
 */

public class RelationServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationService");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.NotificationBroadcasterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationBroadcasterSupport");
        check(Class.forName("javax.management.relation.RelationServiceMBean").isAssignableFrom(c), "implements/extends javax.management.relation.RelationServiceMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("javax.management.NotificationListener").isAssignableFrom(c), "implements/extends javax.management.NotificationListener");
        check(hasMethod(c, "addRelation"), "declares addRelation");
        check(hasMethod(c, "addRelationType"), "declares addRelationType");
        check(hasMethod(c, "checkRoleReading"), "declares checkRoleReading");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
