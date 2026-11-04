/*
 * @test
 * @summary com.sun.jmx.mbeanserver.JmxMBeanServer conformance: hierarchy and declared members via reflection.
 * @run main JmxMBeanServerTest
 */

public class JmxMBeanServerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.JmxMBeanServer"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jmx.mbeanserver.SunJmxMBeanServer"), "implements com.sun.jmx.mbeanserver.SunJmxMBeanServer");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("checkMBeanPermission"), "declares checkMBeanPermission");
        check(ms.contains("checkNewMBeanServerPermission"), "declares checkNewMBeanServerPermission");
        check(ms.contains("cloneAttribute"), "declares cloneAttribute");
        check(ms.contains("cloneAttributeList"), "declares cloneAttributeList");
        check(ms.contains("cloneObjectName"), "declares cloneObjectName");
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
        check(ms.contains("getMBeanInstantiator"), "declares getMBeanInstantiator");
        check(ms.contains("getMBeanServerDelegate"), "declares getMBeanServerDelegate");
        check(ms.contains("getMBeanServerInterceptor"), "declares getMBeanServerInterceptor");
        check(ms.contains("getObjectInstance"), "declares getObjectInstance");
        check(ms.contains("initialize"), "declares initialize");
        check(ms.contains("instantiate"), "declares instantiate");
        check(ms.contains("interceptorsEnabled"), "declares interceptorsEnabled");
        check(ms.contains("invoke"), "declares invoke");
        check(ms.contains("isInstanceOf"), "declares isInstanceOf");
        check(ms.contains("isRegistered"), "declares isRegistered");
        check(ms.contains("newMBeanServer"), "declares newMBeanServer");
        check(ms.contains("newMBeanServerDelegate"), "declares newMBeanServerDelegate");
        check(ms.contains("queryMBeans"), "declares queryMBeans");
        check(ms.contains("queryNames"), "declares queryNames");
        check(ms.contains("registerMBean"), "declares registerMBean");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("setAttribute"), "declares setAttribute");
        check(ms.contains("setAttributes"), "declares setAttributes");
        check(ms.contains("setMBeanServerInterceptor"), "declares setMBeanServerInterceptor");
        check(ms.contains("unregisterMBean"), "declares unregisterMBean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
