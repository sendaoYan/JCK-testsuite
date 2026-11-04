/*
 * @test
 * @summary com.sun.xml.internal.ws.client.Stub conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StubTest
 */

import java.lang.reflect.Modifier;


public class StubTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.Stub", false, StubTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.Stub"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.ComponentRegistry"), "implements com.sun.xml.internal.ws.api.ComponentRegistry");
        check(has(ifs, "com.sun.xml.internal.ws.client.ResponseContextReceiver"), "implements com.sun.xml.internal.ws.client.ResponseContextReceiver");
        check(has(ifs, "com.sun.xml.internal.ws.developer.WSBindingProvider"), "implements com.sun.xml.internal.ws.developer.WSBindingProvider");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("configureFiber"), "method configureFiber");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getEndpointReference"), "method getEndpointReference");
        check(mn.contains("getExecutor"), "method getExecutor");
        check(mn.contains("getInboundHeaders"), "method getInboundHeaders");
        check(mn.contains("getManagedObjectManager"), "method getManagedObjectManager");
        check(mn.contains("getOperationDispatcher"), "method getOperationDispatcher");
        check(mn.contains("getPortInfo"), "method getPortInfo");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getRequestContext"), "method getRequestContext");
        check(mn.contains("getResponseContext"), "method getResponseContext");
        check(mn.contains("getSPI"), "method getSPI");
        check(mn.contains("getService"), "method getService");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getTubes"), "method getTubes");
        check(mn.contains("getWSDLPort"), "method getWSDLPort");
        check(mn.contains("getWSEndpointReference"), "method getWSEndpointReference");
        check(mn.contains("process"), "method process");
        check(mn.contains("processAsync"), "method processAsync");
        check(mn.contains("resetRequestContext"), "method resetRequestContext");
        check(mn.contains("setAddress"), "method setAddress");
        check(mn.contains("setOutboundHeaders"), "method setOutboundHeaders");
        check(mn.contains("setResponseContext"), "method setResponseContext");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("PREVENT_SYNC_START_FOR_ASYNC_INVOKE"), "field PREVENT_SYNC_START_FOR_ASYNC_INVOKE");
        check(fn.contains("addrVersion"), "field addrVersion");
        check(fn.contains("binding"), "field binding");
        check(fn.contains("endpointReference"), "field endpointReference");
        check(fn.contains("operationDispatcher"), "field operationDispatcher");
        check(fn.contains("owner"), "field owner");
        check(fn.contains("portInfo"), "field portInfo");
        check(fn.contains("portname"), "field portname");
        check(fn.contains("requestContext"), "field requestContext");
        check(fn.contains("wsdlPort"), "field wsdlPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
