/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLBoundOperationImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLBoundOperationImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLBoundOperationImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLBoundOperationImpl", false, WSDLBoundOperationImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLBoundOperationImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundOperation"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundOperation");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addExtension"), "method addExtension");
        check(mn.contains("addFault"), "method addFault");
        check(mn.contains("addNotUnderstoodExtension"), "method addNotUnderstoodExtension");
        check(mn.contains("addPart"), "method addPart");
        check(mn.contains("areRequiredExtensionsUnderstood"), "method areRequiredExtensionsUnderstood");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("getAnonymous"), "method getAnonymous");
        check(mn.contains("getBoundPortType"), "method getBoundPortType");
        check(mn.contains("getExtension"), "method getExtension");
        check(mn.contains("getFaultBinding"), "method getFaultBinding");
        check(mn.contains("getFaultParts"), "method getFaultParts");
        check(mn.contains("getFaults"), "method getFaults");
        check(mn.contains("getInParts"), "method getInParts");
        check(mn.contains("getInputBinding"), "method getInputBinding");
        check(mn.contains("getInputParts"), "method getInputParts");
        check(mn.contains("getMimeTypeForFaultPart"), "method getMimeTypeForFaultPart");
        check(mn.contains("getMimeTypeForInputPart"), "method getMimeTypeForInputPart");
        check(mn.contains("getMimeTypeForOutputPart"), "method getMimeTypeForOutputPart");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNotUnderstoodExtensions"), "method getNotUnderstoodExtensions");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOutParts"), "method getOutParts");
        check(mn.contains("getOutputBinding"), "method getOutputBinding");
        check(mn.contains("getOutputParts"), "method getOutputParts");
        check(mn.contains("getPart"), "method getPart");
        check(mn.contains("getRequestNamespace"), "method getRequestNamespace");
        check(mn.contains("getRequestPayloadName"), "method getRequestPayloadName");
        check(mn.contains("getResponseNamespace"), "method getResponseNamespace");
        check(mn.contains("getResponsePayloadName"), "method getResponsePayloadName");
        check(mn.contains("getSOAPAction"), "method getSOAPAction");
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
