/*
 * @test
 * @summary javax.management.relation.RelationNotification conformance: concrete class reflection.
 * @run main RelationNotificationTest
 */

public class RelationNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationNotification");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.Notification".equals(c.getSuperclass().getName()), "superclass javax.management.Notification");
        check(hasMethod(c, "getMBeansToUnregister"), "declares getMBeansToUnregister");
        check(hasMethod(c, "getNewRoleValue"), "declares getNewRoleValue");
        check(hasMethod(c, "getObjectName"), "declares getObjectName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
