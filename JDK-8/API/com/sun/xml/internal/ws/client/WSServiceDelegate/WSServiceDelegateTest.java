/*
 * @test
 * @summary com.sun.xml.internal.ws.client.WSServiceDelegate conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSServiceDelegateTest
 */

import java.lang.reflect.Modifier;


public class WSServiceDelegateTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.WSServiceDelegate", false, WSServiceDelegateTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.WSServiceDelegate"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.WSService"), "superclass com.sun.xml.internal.ws.api.WSService");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addPort"), "method addPort");
        check(mn.contains("buildRuntimeModel"), "method buildRuntimeModel");
        check(mn.contains("createCatalogResolver"), "method createCatalogResolver");
        check(mn.contains("createDispatch"), "method createDispatch");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getEndpointAddress"), "method getEndpointAddress");
        check(mn.contains("getExecutor"), "method getExecutor");
        check(mn.contains("getHandlerResolver"), "method getHandlerResolver");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortModel"), "method getPortModel");
        check(mn.contains("getPorts"), "method getPorts");
        check(mn.contains("getQNameToPortInfoMap"), "method getQNameToPortInfoMap");
        check(mn.contains("getServiceClass"), "method getServiceClass");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getStubHandler"), "method getStubHandler");
        check(mn.contains("getWSDLDocumentLocation"), "method getWSDLDocumentLocation");
        check(mn.contains("getWsdlService"), "method getWsdlService");
        check(mn.contains("safeGetPort"), "method safeGetPort");
        check(mn.contains("setExecutor"), "method setExecutor");
        check(mn.contains("setHandlerResolver"), "method setHandlerResolver");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("EMPTY_FEATURES"), "field EMPTY_FEATURES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
