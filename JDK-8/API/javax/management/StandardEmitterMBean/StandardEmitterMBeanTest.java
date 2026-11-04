/*
 * @test
 * @summary javax.management.StandardEmitterMBean conformance: concrete class reflection.
 * @run main StandardEmitterMBeanTest
 */

public class StandardEmitterMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.StandardEmitterMBean");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.StandardMBean".equals(c.getSuperclass().getName()), "superclass javax.management.StandardMBean");
        check(Class.forName("javax.management.NotificationEmitter").isAssignableFrom(c), "implements/extends javax.management.NotificationEmitter");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "getNotificationInfo"), "declares getNotificationInfo");
        check(hasMethod(c, "removeNotificationListener"), "declares removeNotificationListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
