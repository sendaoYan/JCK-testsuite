/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.wsdl.WSDLModel conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLModelTest
 */

import java.lang.reflect.Modifier;


public class WSDLModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.wsdl.WSDLModel", false, WSDLModelTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.wsdl.WSDLModel"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getBindings"), "method getBindings");
        check(mn.contains("getFirstServiceName"), "method getFirstServiceName");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getMessages"), "method getMessages");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getPortType"), "method getPortType");
        check(mn.contains("getPortTypes"), "method getPortTypes");
        check(mn.contains("getService"), "method getService");
        check(mn.contains("getServices"), "method getServices");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
