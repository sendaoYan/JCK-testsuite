/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.WSDLGenInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLGenInfoTest
 */

import java.lang.reflect.Modifier;


public class WSDLGenInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.WSDLGenInfo", false, WSDLGenInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.WSDLGenInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getExtensions"), "method getExtensions");
        check(mn.contains("getWsdlResolver"), "method getWsdlResolver");
        check(mn.contains("isInlineSchemas"), "method isInlineSchemas");
        check(mn.contains("isSecureXmlProcessingDisabled"), "method isSecureXmlProcessingDisabled");
        check(mn.contains("setContainer"), "method setContainer");
        check(mn.contains("setExtensions"), "method setExtensions");
        check(mn.contains("setInlineSchemas"), "method setInlineSchemas");
        check(mn.contains("setSecureXmlProcessingDisabled"), "method setSecureXmlProcessingDisabled");
        check(mn.contains("setWsdlResolver"), "method setWsdlResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
