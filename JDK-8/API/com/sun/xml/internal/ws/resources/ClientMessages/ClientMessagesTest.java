/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.ClientMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClientMessagesTest
 */

import java.lang.reflect.Modifier;


public class ClientMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.ClientMessages", false, ClientMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.ClientMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("EPR_WITHOUT_ADDRESSING_ON"), "method EPR_WITHOUT_ADDRESSING_ON");
        check(mn.contains("FAILED_TO_PARSE"), "method FAILED_TO_PARSE");
        check(mn.contains("FAILED_TO_PARSE_EPR"), "method FAILED_TO_PARSE_EPR");
        check(mn.contains("FAILED_TO_PARSE_WITH_MEX"), "method FAILED_TO_PARSE_WITH_MEX");
        check(mn.contains("HTTP_CLIENT_FAILED"), "method HTTP_CLIENT_FAILED");
        check(mn.contains("HTTP_NOT_FOUND"), "method HTTP_NOT_FOUND");
        check(mn.contains("HTTP_STATUS_CODE"), "method HTTP_STATUS_CODE");
        check(mn.contains("INVALID_ADDRESS"), "method INVALID_ADDRESS");
        check(mn.contains("INVALID_BINDING_ID"), "method INVALID_BINDING_ID");
        check(mn.contains("INVALID_EPR_PORT_NAME"), "method INVALID_EPR_PORT_NAME");
        check(mn.contains("INVALID_PORT_NAME"), "method INVALID_PORT_NAME");
        check(mn.contains("INVALID_SERVICE_NAME"), "method INVALID_SERVICE_NAME");
        check(mn.contains("INVALID_SERVICE_NAME_NULL"), "method INVALID_SERVICE_NAME_NULL");
        check(mn.contains("INVALID_SERVICE_NO_WSDL"), "method INVALID_SERVICE_NO_WSDL");
        check(mn.contains("INVALID_SOAP_ACTION"), "method INVALID_SOAP_ACTION");
        check(mn.contains("INVALID_SOAP_ROLE_NONE"), "method INVALID_SOAP_ROLE_NONE");
        check(mn.contains("INVALID_WSDL_URL"), "method INVALID_WSDL_URL");
        check(mn.contains("LOCAL_CLIENT_FAILED"), "method LOCAL_CLIENT_FAILED");
        check(mn.contains("NON_LOGICAL_HANDLER_SET"), "method NON_LOGICAL_HANDLER_SET");
        check(mn.contains("RUNTIME_WSDLPARSER_INVALID_WSDL"), "method RUNTIME_WSDLPARSER_INVALID_WSDL");
        check(mn.contains("UNDEFINED_BINDING"), "method UNDEFINED_BINDING");
        check(mn.contains("UNDEFINED_PORT_TYPE"), "method UNDEFINED_PORT_TYPE");
        check(mn.contains("UNSUPPORTED_OPERATION"), "method UNSUPPORTED_OPERATION");
        check(mn.contains("WSDL_CONTAINS_NO_SERVICE"), "method WSDL_CONTAINS_NO_SERVICE");
        check(mn.contains("WSDL_NOT_FOUND"), "method WSDL_NOT_FOUND");
        check(mn.contains("localizableEPR_WITHOUT_ADDRESSING_ON"), "method localizableEPR_WITHOUT_ADDRESSING_ON");
        check(mn.contains("localizableFAILED_TO_PARSE"), "method localizableFAILED_TO_PARSE");
        check(mn.contains("localizableFAILED_TO_PARSE_EPR"), "method localizableFAILED_TO_PARSE_EPR");
        check(mn.contains("localizableFAILED_TO_PARSE_WITH_MEX"), "method localizableFAILED_TO_PARSE_WITH_MEX");
        check(mn.contains("localizableHTTP_CLIENT_FAILED"), "method localizableHTTP_CLIENT_FAILED");
        check(mn.contains("localizableHTTP_NOT_FOUND"), "method localizableHTTP_NOT_FOUND");
        check(mn.contains("localizableHTTP_STATUS_CODE"), "method localizableHTTP_STATUS_CODE");
        check(mn.contains("localizableINVALID_ADDRESS"), "method localizableINVALID_ADDRESS");
        check(mn.contains("localizableINVALID_BINDING_ID"), "method localizableINVALID_BINDING_ID");
        check(mn.contains("localizableINVALID_EPR_PORT_NAME"), "method localizableINVALID_EPR_PORT_NAME");
        check(mn.contains("localizableINVALID_PORT_NAME"), "method localizableINVALID_PORT_NAME");
        check(mn.contains("localizableINVALID_SERVICE_NAME"), "method localizableINVALID_SERVICE_NAME");
        check(mn.contains("localizableINVALID_SERVICE_NAME_NULL"), "method localizableINVALID_SERVICE_NAME_NULL");
        check(mn.contains("localizableINVALID_SERVICE_NO_WSDL"), "method localizableINVALID_SERVICE_NO_WSDL");
        check(mn.contains("localizableINVALID_SOAP_ACTION"), "method localizableINVALID_SOAP_ACTION");
        check(mn.contains("localizableINVALID_SOAP_ROLE_NONE"), "method localizableINVALID_SOAP_ROLE_NONE");
        check(mn.contains("localizableINVALID_WSDL_URL"), "method localizableINVALID_WSDL_URL");
        check(mn.contains("localizableLOCAL_CLIENT_FAILED"), "method localizableLOCAL_CLIENT_FAILED");
        check(mn.contains("localizableNON_LOGICAL_HANDLER_SET"), "method localizableNON_LOGICAL_HANDLER_SET");
        check(mn.contains("localizableRUNTIME_WSDLPARSER_INVALID_WSDL"), "method localizableRUNTIME_WSDLPARSER_INVALID_WSDL");
        check(mn.contains("localizableUNDEFINED_BINDING"), "method localizableUNDEFINED_BINDING");
        check(mn.contains("localizableUNDEFINED_PORT_TYPE"), "method localizableUNDEFINED_PORT_TYPE");
        check(mn.contains("localizableUNSUPPORTED_OPERATION"), "method localizableUNSUPPORTED_OPERATION");
        check(mn.contains("localizableWSDL_CONTAINS_NO_SERVICE"), "method localizableWSDL_CONTAINS_NO_SERVICE");
        check(mn.contains("localizableWSDL_NOT_FOUND"), "method localizableWSDL_NOT_FOUND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
