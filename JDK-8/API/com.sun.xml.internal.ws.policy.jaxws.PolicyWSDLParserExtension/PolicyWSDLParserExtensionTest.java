/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.jaxws.PolicyWSDLParserExtension conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyWSDLParserExtensionTest
 */

import java.lang.reflect.Modifier;


public class PolicyWSDLParserExtensionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.jaxws.PolicyWSDLParserExtension", false, PolicyWSDLParserExtensionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.jaxws.PolicyWSDLParserExtension"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.wsdl.parser.WSDLParserExtension"), "superclass com.sun.xml.internal.ws.api.wsdl.parser.WSDLParserExtension");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("bindingAttributes"), "method bindingAttributes");
        check(mn.contains("bindingElements"), "method bindingElements");
        check(mn.contains("bindingOperationAttributes"), "method bindingOperationAttributes");
        check(mn.contains("bindingOperationElements"), "method bindingOperationElements");
        check(mn.contains("bindingOperationFaultAttributes"), "method bindingOperationFaultAttributes");
        check(mn.contains("bindingOperationFaultElements"), "method bindingOperationFaultElements");
        check(mn.contains("bindingOperationInputAttributes"), "method bindingOperationInputAttributes");
        check(mn.contains("bindingOperationInputElements"), "method bindingOperationInputElements");
        check(mn.contains("bindingOperationOutputAttributes"), "method bindingOperationOutputAttributes");
        check(mn.contains("bindingOperationOutputElements"), "method bindingOperationOutputElements");
        check(mn.contains("definitionsElements"), "method definitionsElements");
        check(mn.contains("finished"), "method finished");
        check(mn.contains("messageAttributes"), "method messageAttributes");
        check(mn.contains("messageElements"), "method messageElements");
        check(mn.contains("portAttributes"), "method portAttributes");
        check(mn.contains("portElements"), "method portElements");
        check(mn.contains("portTypeAttributes"), "method portTypeAttributes");
        check(mn.contains("portTypeElements"), "method portTypeElements");
        check(mn.contains("portTypeOperationAttributes"), "method portTypeOperationAttributes");
        check(mn.contains("portTypeOperationElements"), "method portTypeOperationElements");
        check(mn.contains("portTypeOperationFaultAttributes"), "method portTypeOperationFaultAttributes");
        check(mn.contains("portTypeOperationFaultElements"), "method portTypeOperationFaultElements");
        check(mn.contains("portTypeOperationInputAttributes"), "method portTypeOperationInputAttributes");
        check(mn.contains("portTypeOperationInputElements"), "method portTypeOperationInputElements");
        check(mn.contains("portTypeOperationOutputAttributes"), "method portTypeOperationOutputAttributes");
        check(mn.contains("portTypeOperationOutputElements"), "method portTypeOperationOutputElements");
        check(mn.contains("postFinished"), "method postFinished");
        check(mn.contains("serviceAttributes"), "method serviceAttributes");
        check(mn.contains("serviceElements"), "method serviceElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
