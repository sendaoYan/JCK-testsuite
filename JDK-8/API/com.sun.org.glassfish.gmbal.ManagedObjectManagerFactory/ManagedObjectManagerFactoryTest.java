/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.ManagedObjectManagerFactory conformance: reflection.
 * @run main ManagedObjectManagerFactoryTest
 */

public class ManagedObjectManagerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.ManagedObjectManagerFactory", false, ManagedObjectManagerFactoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ManagedObjectManagerFactory"), "simple name ManagedObjectManagerFactory");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getMethod"), "method getMethod");
        check(has(c, "createStandalone"), "method createStandalone");
        check(has(c, "createFederated"), "method createFederated");
        check(has(c, "createNOOP"), "method createNOOP");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
