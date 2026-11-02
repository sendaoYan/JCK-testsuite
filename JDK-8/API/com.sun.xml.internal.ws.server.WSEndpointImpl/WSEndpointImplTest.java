/*
 * @test
 * @summary com.sun.xml.internal.ws.server.WSEndpointImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSEndpointImplTest
 */

import java.lang.reflect.Modifier;


public class WSEndpointImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.WSEndpointImpl", false, WSEndpointImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.WSEndpointImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.server.WSEndpoint"), "superclass com.sun.xml.internal.ws.api.server.WSEndpoint");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.server.LazyMOMProvider$WSEndpointScopeChangeListener"), "implements com.sun.xml.internal.ws.api.server.LazyMOMProvider$WSEndpointScopeChangeListener");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("closeManagedObjectManager"), "method closeManagedObjectManager");
        check(mn.contains("createCodec"), "method createCodec");
        check(mn.contains("createPipeHead"), "method createPipeHead");
        check(mn.contains("createServerTubeAssemblerContext"), "method createServerTubeAssemblerContext");
        check(mn.contains("createServiceResponseForException"), "method createServiceResponseForException");
        check(mn.contains("dispose"), "method dispose");
        check(mn.contains("getAssemblerContext"), "method getAssemblerContext");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getComponentRegistry"), "method getComponentRegistry");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getEndpointReference"), "method getEndpointReference");
        check(mn.contains("getEndpointReferenceExtensions"), "method getEndpointReferenceExtensions");
        check(mn.contains("getEngine"), "method getEngine");
        check(mn.contains("getImplementationClass"), "method getImplementationClass");
        check(mn.contains("getManagedObjectManager"), "method getManagedObjectManager");
        check(mn.contains("getOperationDispatcher"), "method getOperationDispatcher");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getServiceDefinition"), "method getServiceDefinition");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("process"), "method process");
        check(mn.contains("schedule"), "method schedule");
        check(mn.contains("scopeChanged"), "method scopeChanged");
        check(mn.contains("setExecutor"), "method setExecutor");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("binding"), "field binding");
        check(fn.contains("masterTubeline"), "field masterTubeline");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
