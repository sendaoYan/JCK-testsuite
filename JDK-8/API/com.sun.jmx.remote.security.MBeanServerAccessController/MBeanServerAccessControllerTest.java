/*
 * @test
 * @summary com.sun.jmx.remote.security.MBeanServerAccessController conformance: hierarchy and declared members via reflection.
 * @run main MBeanServerAccessControllerTest
 */

public class MBeanServerAccessControllerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.MBeanServerAccessController", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.MBeanServerAccessController"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.remote.MBeanServerForwarder"), "implements javax.management.remote.MBeanServerForwarder");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("checkClassLoader"), "declares checkClassLoader");
        check(ms.contains("checkCreate"), "declares checkCreate");
        check(ms.contains("checkMLetMethods"), "declares checkMLetMethods");
        check(ms.contains("checkRead"), "declares checkRead");
        check(ms.contains("checkUnregister"), "declares checkUnregister");
        check(ms.contains("checkWrite"), "declares checkWrite");
        check(ms.contains("createMBean"), "declares createMBean");
        check(ms.contains("deserialize"), "declares deserialize");
        check(ms.contains("getAttribute"), "declares getAttribute");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getClassLoader"), "declares getClassLoader");
        check(ms.contains("getClassLoaderFor"), "declares getClassLoaderFor");
        check(ms.contains("getClassLoaderRepository"), "declares getClassLoaderRepository");
        check(ms.contains("getDefaultDomain"), "declares getDefaultDomain");
        check(ms.contains("getDomains"), "declares getDomains");
        check(ms.contains("getMBeanCount"), "declares getMBeanCount");
        check(ms.contains("getMBeanInfo"), "declares getMBeanInfo");
        check(ms.contains("getMBeanServer"), "declares getMBeanServer");
        check(ms.contains("getObjectInstance"), "declares getObjectInstance");
        check(ms.contains("instantiate"), "declares instantiate");
        check(ms.contains("invoke"), "declares invoke");
        check(ms.contains("isInstanceOf"), "declares isInstanceOf");
        check(ms.contains("isRegistered"), "declares isRegistered");
        check(ms.contains("queryMBeans"), "declares queryMBeans");
        check(ms.contains("queryNames"), "declares queryNames");
        check(ms.contains("registerMBean"), "declares registerMBean");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("setAttribute"), "declares setAttribute");
        check(ms.contains("setAttributes"), "declares setAttributes");
        check(ms.contains("setMBeanServer"), "declares setMBeanServer");
        check(ms.contains("unregisterMBean"), "declares unregisterMBean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
