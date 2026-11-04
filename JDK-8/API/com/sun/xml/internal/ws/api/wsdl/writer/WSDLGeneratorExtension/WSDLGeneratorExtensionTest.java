/*
 * @test
 * @summary com.sun.xml.internal.ws.api.wsdl.writer.WSDLGeneratorExtension conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLGeneratorExtensionTest
 */

import java.lang.reflect.Modifier;


public class WSDLGeneratorExtensionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.wsdl.writer.WSDLGeneratorExtension", false, WSDLGeneratorExtensionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.wsdl.writer.WSDLGeneratorExtension"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBindingExtension"), "method addBindingExtension");
        check(mn.contains("addBindingOperationExtension"), "method addBindingOperationExtension");
        check(mn.contains("addBindingOperationFaultExtension"), "method addBindingOperationFaultExtension");
        check(mn.contains("addBindingOperationInputExtension"), "method addBindingOperationInputExtension");
        check(mn.contains("addBindingOperationOutputExtension"), "method addBindingOperationOutputExtension");
        check(mn.contains("addDefinitionsExtension"), "method addDefinitionsExtension");
        check(mn.contains("addFaultMessageExtension"), "method addFaultMessageExtension");
        check(mn.contains("addInputMessageExtension"), "method addInputMessageExtension");
        check(mn.contains("addOperationExtension"), "method addOperationExtension");
        check(mn.contains("addOperationFaultExtension"), "method addOperationFaultExtension");
        check(mn.contains("addOperationInputExtension"), "method addOperationInputExtension");
        check(mn.contains("addOperationOutputExtension"), "method addOperationOutputExtension");
        check(mn.contains("addOutputMessageExtension"), "method addOutputMessageExtension");
        check(mn.contains("addPortExtension"), "method addPortExtension");
        check(mn.contains("addPortTypeExtension"), "method addPortTypeExtension");
        check(mn.contains("addServiceExtension"), "method addServiceExtension");
        check(mn.contains("end"), "method end");
        check(mn.contains("start"), "method start");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
