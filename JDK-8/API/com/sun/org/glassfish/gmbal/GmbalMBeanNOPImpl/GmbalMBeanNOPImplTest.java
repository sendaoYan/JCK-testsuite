/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.GmbalMBeanNOPImpl conformance: reflection.
 * @run main GmbalMBeanNOPImplTest
 */

public class GmbalMBeanNOPImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.GmbalMBeanNOPImpl", false, GmbalMBeanNOPImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("GmbalMBeanNOPImpl"), "simple name GmbalMBeanNOPImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.gmbal.GmbalMBean"), "interface com.sun.org.glassfish.gmbal.GmbalMBean");
        check(has(c, "getAttribute"), "method getAttribute");
        check(has(c, "setAttribute"), "method setAttribute");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "setAttributes"), "method setAttributes");
        check(has(c, "invoke"), "method invoke");
        check(has(c, "getMBeanInfo"), "method getMBeanInfo");
        check(has(c, "removeNotificationListener"), "method removeNotificationListener");
        check(has(c, "addNotificationListener"), "method addNotificationListener");
        check(has(c, "getNotificationInfo"), "method getNotificationInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
