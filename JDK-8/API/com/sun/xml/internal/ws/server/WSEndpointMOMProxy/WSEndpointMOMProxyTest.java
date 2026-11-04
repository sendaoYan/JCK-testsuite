/*
 * @test
 * @summary com.sun.xml.internal.ws.server.WSEndpointMOMProxy conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSEndpointMOMProxyTest
 */

import java.lang.reflect.Modifier;


public class WSEndpointMOMProxyTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.WSEndpointMOMProxy", false, WSEndpointMOMProxyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.WSEndpointMOMProxy"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.server.WSEndpoint"), "superclass com.sun.xml.internal.ws.api.server.WSEndpoint");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.org.glassfish.gmbal.ManagedObjectManager"), "implements com.sun.org.glassfish.gmbal.ManagedObjectManager");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAnnotation"), "method addAnnotation");
        check(mn.contains("close"), "method close");
        check(mn.contains("closeManagedObjectManager"), "method closeManagedObjectManager");
        check(mn.contains("createCodec"), "method createCodec");
        check(mn.contains("createPipeHead"), "method createPipeHead");
        check(mn.contains("createRoot"), "method createRoot");
        check(mn.contains("createServiceResponseForException"), "method createServiceResponseForException");
        check(mn.contains("dispose"), "method dispose");
        check(mn.contains("dumpSkeleton"), "method dumpSkeleton");
        check(mn.contains("equalsProxiedInstance"), "method equalsProxiedInstance");
        check(mn.contains("getAMXClient"), "method getAMXClient");
        check(mn.contains("getAssemblerContext"), "method getAssemblerContext");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getComponentRegistry"), "method getComponentRegistry");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getDomain"), "method getDomain");
        check(mn.contains("getEndpointReference"), "method getEndpointReference");
        check(mn.contains("getImplementationClass"), "method getImplementationClass");
        check(mn.contains("getMBeanServer"), "method getMBeanServer");
        check(mn.contains("getManagedObjectManager"), "method getManagedObjectManager");
        check(mn.contains("getObject"), "method getObject");
        check(mn.contains("getObjectName"), "method getObjectName");
        check(mn.contains("getOperationDispatcher"), "method getOperationDispatcher");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getResourceBundle"), "method getResourceBundle");
        check(mn.contains("getRoot"), "method getRoot");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getServiceDefinition"), "method getServiceDefinition");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getWsEndpoint"), "method getWsEndpoint");
        check(mn.contains("isInitialized"), "method isInitialized");
        check(mn.contains("isManagedObject"), "method isManagedObject");
        check(mn.contains("register"), "method register");
        check(mn.contains("registerAtRoot"), "method registerAtRoot");
        check(mn.contains("resumeJMXRegistration"), "method resumeJMXRegistration");
        check(mn.contains("schedule"), "method schedule");
        check(mn.contains("setExecutor"), "method setExecutor");
        check(mn.contains("setJMXRegistrationDebug"), "method setJMXRegistrationDebug");
        check(mn.contains("setMBeanServer"), "method setMBeanServer");
        check(mn.contains("setRegistrationDebug"), "method setRegistrationDebug");
        check(mn.contains("setResourceBundle"), "method setResourceBundle");
        check(mn.contains("setRuntimeDebug"), "method setRuntimeDebug");
        check(mn.contains("setTypelibDebug"), "method setTypelibDebug");
        check(mn.contains("stripPackagePrefix"), "method stripPackagePrefix");
        check(mn.contains("stripPrefix"), "method stripPrefix");
        check(mn.contains("suppressDuplicateRootReport"), "method suppressDuplicateRootReport");
        check(mn.contains("suspendJMXRegistration"), "method suspendJMXRegistration");
        check(mn.contains("unregister"), "method unregister");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
