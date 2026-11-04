/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl.UnknownWSDLExtension conformance: reflection (kind, superclass, interfaces, methods).
 * @run main UnknownWSDLExtensionTest
 */

import java.lang.reflect.Modifier;


public class UnknownWSDLExtensionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl$UnknownWSDLExtension", false, UnknownWSDLExtensionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl$UnknownWSDLExtension"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLExtension"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLExtension");
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLObject"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLObject");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLocation"), "method getLocation");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
