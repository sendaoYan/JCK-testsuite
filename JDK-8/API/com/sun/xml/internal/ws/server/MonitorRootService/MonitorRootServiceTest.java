/*
 * @test
 * @summary com.sun.xml.internal.ws.server.MonitorRootService conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MonitorRootServiceTest
 */

import java.lang.reflect.Modifier;


public class MonitorRootServiceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.MonitorRootService", false, MonitorRootServiceTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.MonitorRootService"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.server.MonitorBase"), "superclass com.sun.xml.internal.ws.server.MonitorBase");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addressingVersion"), "method addressingVersion");
        check(mn.contains("bindingID"), "method bindingID");
        check(mn.contains("container"), "method container");
        check(mn.contains("dumpHTTPMessages"), "method dumpHTTPMessages");
        check(mn.contains("features"), "method features");
        check(mn.contains("jaxwsRuntimeVersion"), "method jaxwsRuntimeVersion");
        check(mn.contains("policy"), "method policy");
        check(mn.contains("portName"), "method portName");
        check(mn.contains("seiModelWSDLLocation"), "method seiModelWSDLLocation");
        check(mn.contains("serviceDefinitionImports"), "method serviceDefinitionImports");
        check(mn.contains("serviceDefinitionURL"), "method serviceDefinitionURL");
        check(mn.contains("serviceName"), "method serviceName");
        check(mn.contains("soapVersionHttpBindingId"), "method soapVersionHttpBindingId");
        check(mn.contains("wsdlEndpointAddress"), "method wsdlEndpointAddress");
        check(mn.contains("wsdlPortTypeName"), "method wsdlPortTypeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
