/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundOperation conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EditableWSDLBoundOperationTest
 */

import java.lang.reflect.Modifier;


public class EditableWSDLBoundOperationTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundOperation", false, EditableWSDLBoundOperationTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundOperation"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addFault"), "method addFault");
        check(mn.contains("addPart"), "method addPart");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("getBoundPortType"), "method getBoundPortType");
        check(mn.contains("getFaults"), "method getFaults");
        check(mn.contains("getInParts"), "method getInParts");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOutParts"), "method getOutParts");
        check(mn.contains("getPart"), "method getPart");
        check(mn.contains("setAnonymous"), "method setAnonymous");
        check(mn.contains("setFaultExplicitBodyParts"), "method setFaultExplicitBodyParts");
        check(mn.contains("setInputExplicitBodyParts"), "method setInputExplicitBodyParts");
        check(mn.contains("setOutputExplicitBodyParts"), "method setOutputExplicitBodyParts");
        check(mn.contains("setRequestNamespace"), "method setRequestNamespace");
        check(mn.contains("setResponseNamespace"), "method setResponseNamespace");
        check(mn.contains("setSoapAction"), "method setSoapAction");
        check(mn.contains("setStyle"), "method setStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
