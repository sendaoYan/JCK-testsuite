/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundPortType conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLBoundPortTypeTest
 */

import java.lang.reflect.Modifier;


public class WSDLBoundPortTypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundPortType", false, WSDLBoundPortTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundPortType"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible");
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLFeaturedObject"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLFeaturedObject");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "method get");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getBindingId"), "method getBindingId");
        check(mn.contains("getBindingOperations"), "method getBindingOperations");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getPortType"), "method getPortType");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getStyle"), "method getStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
