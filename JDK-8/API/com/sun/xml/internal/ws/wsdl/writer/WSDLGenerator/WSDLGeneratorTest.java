/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLGeneratorTest
 */

import java.lang.reflect.Modifier;


public class WSDLGeneratorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator", false, WSDLGeneratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createOutputFile"), "method createOutputFile");
        check(mn.contains("doGeneration"), "method doGeneration");
        check(mn.contains("generateBinding"), "method generateBinding");
        check(mn.contains("generateBindingOperation"), "method generateBindingOperation");
        check(mn.contains("generateDocumentParameterOrder"), "method generateDocumentParameterOrder");
        check(mn.contains("generateInputMessage"), "method generateInputMessage");
        check(mn.contains("generateMessages"), "method generateMessages");
        check(mn.contains("generateOutputMessage"), "method generateOutputMessage");
        check(mn.contains("generateParameterOrder"), "method generateParameterOrder");
        check(mn.contains("generatePortType"), "method generatePortType");
        check(mn.contains("generateRpcParameterOrder"), "method generateRpcParameterOrder");
        check(mn.contains("generateSOAP12BindingOperation"), "method generateSOAP12BindingOperation");
        check(mn.contains("generateSOAP12Headers"), "method generateSOAP12Headers");
        check(mn.contains("generateSOAPHeaders"), "method generateSOAPHeaders");
        check(mn.contains("generateSOAPMessages"), "method generateSOAPMessages");
        check(mn.contains("generateService"), "method generateService");
        check(mn.contains("generateTypes"), "method generateTypes");
        check(mn.contains("isAttachmentParameter"), "method isAttachmentParameter");
        check(mn.contains("isBodyParameter"), "method isBodyParameter");
        check(mn.contains("isHeaderParameter"), "method isHeaderParameter");
        check(mn.contains("isRpcLit"), "method isRpcLit");
        check(mn.contains("isWrapperStyle"), "method isWrapperStyle");
        check(mn.contains("mangleName"), "method mangleName");
        check(mn.contains("relativize"), "method relativize");
        check(mn.contains("setEndpointAddress"), "method setEndpointAddress");
        check(mn.contains("sortMethodParameters"), "method sortMethodParameters");
        check(mn.contains("splitParameters"), "method splitParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
