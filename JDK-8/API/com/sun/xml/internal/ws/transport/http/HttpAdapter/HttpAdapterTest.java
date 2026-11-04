/*
 * @test
 * @summary com.sun.xml.internal.ws.transport.http.HttpAdapter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HttpAdapterTest
 */

import java.lang.reflect.Modifier;


public class HttpAdapterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.transport.http.HttpAdapter", false, HttpAdapterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.transport.http.HttpAdapter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.server.Adapter"), "superclass com.sun.xml.internal.ws.api.server.Adapter");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSatellites"), "method addSatellites");
        check(mn.contains("createAlone"), "method createAlone");
        check(mn.contains("createToolkit"), "method createToolkit");
        check(mn.contains("fixQuotesAroundSoapAction"), "method fixQuotesAroundSoapAction");
        check(mn.contains("getDocumentAddressResolver"), "method getDocumentAddressResolver");
        check(mn.contains("getNonAnonymousResponseProcessor"), "method getNonAnonymousResponseProcessor");
        check(mn.contains("getPortAddressResolver"), "method getPortAddressResolver");
        check(mn.contains("getServiceDefinition"), "method getServiceDefinition");
        check(mn.contains("getValidPath"), "method getValidPath");
        check(mn.contains("handle"), "method handle");
        check(mn.contains("handleGet"), "method handleGet");
        check(mn.contains("initWSDLMap"), "method initWSDLMap");
        check(mn.contains("invokeAsync"), "method invokeAsync");
        check(mn.contains("publishWSDL"), "method publishWSDL");
        check(mn.contains("setDump"), "method setDump");
        check(mn.contains("setPublishStatus"), "method setPublishStatus");
        check(mn.contains("writeClientError"), "method writeClientError");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("NO_OP_COMPLETION_CALLBACK"), "field NO_OP_COMPLETION_CALLBACK");
        check(fn.contains("disableJreplicaCookie"), "field disableJreplicaCookie");
        check(fn.contains("dump"), "field dump");
        check(fn.contains("dump_threshold"), "field dump_threshold");
        check(fn.contains("owner"), "field owner");
        check(fn.contains("publishStatusPage"), "field publishStatusPage");
        check(fn.contains("stickyCookie"), "field stickyCookie");
        check(fn.contains("urlPattern"), "field urlPattern");
        check(fn.contains("wsdls"), "field wsdls");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
