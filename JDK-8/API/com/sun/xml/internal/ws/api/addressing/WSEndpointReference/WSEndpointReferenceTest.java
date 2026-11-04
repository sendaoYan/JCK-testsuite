/*
 * @test
 * @summary com.sun.xml.internal.ws.api.addressing.WSEndpointReference conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSEndpointReferenceTest
 */

import java.lang.reflect.Modifier;


public class WSEndpointReferenceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.addressing.WSEndpointReference", false, WSEndpointReferenceTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.addressing.WSEndpointReference"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLExtension"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLExtension");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addReferenceParameters"), "method addReferenceParameters");
        check(mn.contains("addReferenceParametersToList"), "method addReferenceParametersToList");
        check(mn.contains("asSource"), "method asSource");
        check(mn.contains("create"), "method create");
        check(mn.contains("createDispatch"), "method createDispatch");
        check(mn.contains("createHeader"), "method createHeader");
        check(mn.contains("createWithAddress"), "method createWithAddress");
        check(mn.contains("getAddress"), "method getAddress");
        check(mn.contains("getEPRExtension"), "method getEPRExtension");
        check(mn.contains("getEPRExtensions"), "method getEPRExtensions");
        check(mn.contains("getMetaData"), "method getMetaData");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("isAnonymous"), "method isAnonymous");
        check(mn.contains("isNone"), "method isNone");
        check(mn.contains("read"), "method read");
        check(mn.contains("toSpec"), "method toSpec");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
