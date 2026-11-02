/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.W3CAddressingWSDLGeneratorExtension conformance: reflection (kind, superclass, interfaces, methods).
 * @run main W3CAddressingWSDLGeneratorExtensionTest
 */

import java.lang.reflect.Modifier;


public class W3CAddressingWSDLGeneratorExtensionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.W3CAddressingWSDLGeneratorExtension", false, W3CAddressingWSDLGeneratorExtensionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.W3CAddressingWSDLGeneratorExtension"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.wsdl.writer.WSDLGeneratorExtension"), "superclass com.sun.xml.internal.ws.api.wsdl.writer.WSDLGeneratorExtension");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBindingExtension"), "method addBindingExtension");
        check(mn.contains("addOperationFaultExtension"), "method addOperationFaultExtension");
        check(mn.contains("addOperationInputExtension"), "method addOperationInputExtension");
        check(mn.contains("addOperationOutputExtension"), "method addOperationOutputExtension");
        check(mn.contains("getDefaultAction"), "method getDefaultAction");
        check(mn.contains("start"), "method start");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
