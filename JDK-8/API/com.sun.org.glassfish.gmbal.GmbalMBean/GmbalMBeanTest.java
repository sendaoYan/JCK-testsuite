/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.GmbalMBean conformance: reflection.
 * @run main GmbalMBeanTest
 */

public class GmbalMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.GmbalMBean", false, GmbalMBeanTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("GmbalMBean"), "simple name GmbalMBean");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.management.DynamicMBean"), "interface javax.management.DynamicMBean");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.management.NotificationEmitter"), "interface javax.management.NotificationEmitter");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
