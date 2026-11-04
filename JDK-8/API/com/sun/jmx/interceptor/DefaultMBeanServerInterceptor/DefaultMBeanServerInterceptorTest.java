/*
 * @test
 * @summary com.sun.jmx.interceptor.DefaultMBeanServerInterceptor conformance: hierarchy and declared members via reflection.
 * @run main DefaultMBeanServerInterceptorTest
 */

public class DefaultMBeanServerInterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor", false, null);
        check(c.getName().equals("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jmx.interceptor.MBeanServerInterceptor"), "implements com.sun.jmx.interceptor.MBeanServerInterceptor");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addClassLoader"), "declares addClassLoader");
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("checkMBeanPermission"), "declares checkMBeanPermission");
        check(ms.contains("checkMBeanTrustPermission"), "declares checkMBeanTrustPermission");
        check(ms.contains("createClassLoaderContext"), "declares createClassLoaderContext");
        check(ms.contains("createMBean"), "declares createMBean");
        check(ms.contains("deserialize"), "declares deserialize");
        check(ms.contains("exclusiveUnregisterMBean"), "declares exclusiveUnregisterMBean");
        check(ms.contains("filterListOfObjectInstances"), "declares filterListOfObjectInstances");
        check(ms.contains("getAttribute"), "declares getAttribute");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getClassLoader"), "declares getClassLoader");
        check(ms.contains("getClassLoaderFor"), "declares getClassLoaderFor");
        check(ms.contains("getClassLoaderRepository"), "declares getClassLoaderRepository");
        check(ms.contains("getClassName"), "declares getClassName");
        check(ms.contains("getDefaultDomain"), "declares getDefaultDomain");
        check(ms.contains("getDomains"), "declares getDomains");
        check(ms.contains("getInstantiatorCLR"), "declares getInstantiatorCLR");
        check(ms.contains("getListener"), "declares getListener");
        check(ms.contains("getListenerWrapper"), "declares getListenerWrapper");
        check(ms.contains("getMBean"), "declares getMBean");
        check(ms.contains("getMBeanCount"), "declares getMBeanCount");
        check(ms.contains("getMBeanInfo"), "declares getMBeanInfo");
        check(ms.contains("getNewMBeanClassName"), "declares getNewMBeanClassName");
        check(ms.contains("getNotificationBroadcaster"), "declares getNotificationBroadcaster");
        check(ms.contains("getObjectInstance"), "declares getObjectInstance");
        check(ms.contains("getResource"), "declares getResource");
        check(ms.contains("instantiate"), "declares instantiate");
        check(ms.contains("invoke"), "declares invoke");
        check(ms.contains("isInstanceOf"), "declares isInstanceOf");
        check(ms.contains("isRegistered"), "declares isRegistered");
        check(ms.contains("makeResourceContextFor"), "declares makeResourceContextFor");
        check(ms.contains("nonDefaultDomain"), "declares nonDefaultDomain");
        check(ms.contains("objectInstancesFromFilteredNamedObjects"), "declares objectInstancesFromFilteredNamedObjects");
        check(ms.contains("objectNamesFromFilteredNamedObjects"), "declares objectNamesFromFilteredNamedObjects");
        check(ms.contains("postDeregisterInvoke"), "declares postDeregisterInvoke");
        check(ms.contains("postRegister"), "declares postRegister");
        check(ms.contains("preDeregisterInvoke"), "declares preDeregisterInvoke");
        check(ms.contains("preRegister"), "declares preRegister");
        check(ms.contains("queryMBeans"), "declares queryMBeans");
        check(ms.contains("queryMBeansImpl"), "declares queryMBeansImpl");
        check(ms.contains("queryNames"), "declares queryNames");
        check(ms.contains("queryNamesImpl"), "declares queryNamesImpl");
        check(ms.contains("registerDynamicMBean"), "declares registerDynamicMBean");
        check(ms.contains("registerMBean"), "declares registerMBean");
        check(ms.contains("registerObject"), "declares registerObject");
        check(ms.contains("registerWithRepository"), "declares registerWithRepository");
        check(ms.contains("removeClassLoader"), "declares removeClassLoader");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("rethrow"), "declares rethrow");
        check(ms.contains("rethrowMaybeMBeanException"), "declares rethrowMaybeMBeanException");
        check(ms.contains("safeGetClassName"), "declares safeGetClassName");
        check(ms.contains("sendNotification"), "declares sendNotification");
        check(ms.contains("setAttribute"), "declares setAttribute");
        check(ms.contains("setAttributes"), "declares setAttributes");
        check(ms.contains("throwMBeanRegistrationException"), "declares throwMBeanRegistrationException");
        check(ms.contains("unregisterFromRepository"), "declares unregisterFromRepository");
        check(ms.contains("unregisterMBean"), "declares unregisterMBean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
