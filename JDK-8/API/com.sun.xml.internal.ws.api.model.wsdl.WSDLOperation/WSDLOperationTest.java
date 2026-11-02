/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.wsdl.WSDLOperation conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLOperationTest
 */

import java.lang.reflect.Modifier;


public class WSDLOperationTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.wsdl.WSDLOperation", false, WSDLOperationTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.wsdl.WSDLOperation"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLExtensible");
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLObject"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLObject");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getFault"), "method getFault");
        check(mn.contains("getFaults"), "method getFaults");
        check(mn.contains("getInput"), "method getInput");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getOutput"), "method getOutput");
        check(mn.contains("getParameterOrder"), "method getParameterOrder");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("isOneWay"), "method isOneWay");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
