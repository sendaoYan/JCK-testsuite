/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.ManagedObjectManager conformance: reflection.
 * @run main ManagedObjectManagerTest
 */

public class ManagedObjectManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.ManagedObjectManager", false, ManagedObjectManagerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ManagedObjectManager"), "simple name ManagedObjectManager");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Closeable"), "interface java.io.Closeable");
        check(has(c, "suspendJMXRegistration"), "method suspendJMXRegistration");
        check(has(c, "resumeJMXRegistration"), "method resumeJMXRegistration");
        check(has(c, "isManagedObject"), "method isManagedObject");
        check(has(c, "createRoot"), "method createRoot");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "register"), "method register");
        check(has(c, "registerAtRoot"), "method registerAtRoot");
        check(has(c, "unregister"), "method unregister");
        check(has(c, "getObjectName"), "method getObjectName");
        check(has(c, "getAMXClient"), "method getAMXClient");
        check(has(c, "getObject"), "method getObject");
        check(has(c, "stripPrefix"), "method stripPrefix");
        check(has(c, "stripPackagePrefix"), "method stripPackagePrefix");
        check(has(c, "getDomain"), "method getDomain");
        check(has(c, "setMBeanServer"), "method setMBeanServer");
        check(has(c, "getMBeanServer"), "method getMBeanServer");
        check(has(c, "setResourceBundle"), "method setResourceBundle");
        check(has(c, "getResourceBundle"), "method getResourceBundle");
        check(has(c, "addAnnotation"), "method addAnnotation");
        check(has(c, "setRegistrationDebug"), "method setRegistrationDebug");
        check(has(c, "setRuntimeDebug"), "method setRuntimeDebug");
        check(has(c, "setTypelibDebug"), "method setTypelibDebug");
        check(has(c, "setJMXRegistrationDebug"), "method setJMXRegistrationDebug");
        check(has(c, "dumpSkeleton"), "method dumpSkeleton");
        check(has(c, "suppressDuplicateRootReport"), "method suppressDuplicateRootReport");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
