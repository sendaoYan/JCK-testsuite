/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.ServerMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ServerMessagesTest
 */

import java.lang.reflect.Modifier;


public class ServerMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.ServerMessages", false, ServerMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.ServerMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("ALREADY_HTTPS_SERVER"), "method ALREADY_HTTPS_SERVER");
        check(mn.contains("ALREADY_HTTP_SERVER"), "method ALREADY_HTTP_SERVER");
        check(mn.contains("ANNOTATION_ONLY_ONCE"), "method ANNOTATION_ONLY_ONCE");
        check(mn.contains("CAN_NOT_GENERATE_WSDL"), "method CAN_NOT_GENERATE_WSDL");
        check(mn.contains("DD_MTOM_CONFLICT"), "method DD_MTOM_CONFLICT");
        check(mn.contains("DISPATCH_CANNOT_FIND_METHOD"), "method DISPATCH_CANNOT_FIND_METHOD");
        check(mn.contains("DUPLICATE_ABSTRACT_WSDL"), "method DUPLICATE_ABSTRACT_WSDL");
        check(mn.contains("DUPLICATE_PORT_KNOWN_HEADER"), "method DUPLICATE_PORT_KNOWN_HEADER");
        check(mn.contains("DUPLICATE_PRIMARY_WSDL"), "method DUPLICATE_PRIMARY_WSDL");
        check(mn.contains("FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER"), "method FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER");
        check(mn.contains("GENERATE_NON_STANDARD_WSDL"), "method GENERATE_NON_STANDARD_WSDL");
        check(mn.contains("NON_UNIQUE_DISPATCH_QNAME"), "method NON_UNIQUE_DISPATCH_QNAME");
        check(mn.contains("NOT_IMPLEMENT_PROVIDER"), "method NOT_IMPLEMENT_PROVIDER");
        check(mn.contains("NOT_KNOW_HTTP_CONTEXT_TYPE"), "method NOT_KNOW_HTTP_CONTEXT_TYPE");
        check(mn.contains("NOT_ZERO_PARAMETERS"), "method NOT_ZERO_PARAMETERS");
        check(mn.contains("NO_CONTENT_TYPE"), "method NO_CONTENT_TYPE");
        check(mn.contains("NO_CURRENT_PACKET"), "method NO_CURRENT_PACKET");
        check(mn.contains("NULL_IMPLEMENTOR"), "method NULL_IMPLEMENTOR");
        check(mn.contains("PORT_NAME_REQUIRED"), "method PORT_NAME_REQUIRED");
        check(mn.contains("PROVIDER_INVALID_PARAMETER_TYPE"), "method PROVIDER_INVALID_PARAMETER_TYPE");
        check(mn.contains("PROVIDER_NOT_PARAMETERIZED"), "method PROVIDER_NOT_PARAMETERIZED");
        check(mn.contains("RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL"), "method RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL");
        check(mn.contains("RUNTIME_PARSER_CLASS_NOT_FOUND"), "method RUNTIME_PARSER_CLASS_NOT_FOUND");
        check(mn.contains("RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE"), "method RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE");
        check(mn.contains("RUNTIME_PARSER_INVALID_ELEMENT"), "method RUNTIME_PARSER_INVALID_ELEMENT");
        check(mn.contains("RUNTIME_PARSER_INVALID_VERSION_NUMBER"), "method RUNTIME_PARSER_INVALID_VERSION_NUMBER");
        check(mn.contains("RUNTIME_PARSER_MISSING_ATTRIBUTE"), "method RUNTIME_PARSER_MISSING_ATTRIBUTE");
        check(mn.contains("RUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE"), "method RUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE");
        check(mn.contains("RUNTIME_PARSER_UNEXPECTED_CONTENT"), "method RUNTIME_PARSER_UNEXPECTED_CONTENT");
        check(mn.contains("RUNTIME_PARSER_WRONG_ELEMENT"), "method RUNTIME_PARSER_WRONG_ELEMENT");
        check(mn.contains("RUNTIME_PARSER_WSDL"), "method RUNTIME_PARSER_WSDL");
        check(mn.contains("RUNTIME_PARSER_WSDL_INCORRECTSERVICE"), "method RUNTIME_PARSER_WSDL_INCORRECTSERVICE");
        check(mn.contains("RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT"), "method RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT");
        check(mn.contains("RUNTIME_PARSER_WSDL_MULTIPLEBINDING"), "method RUNTIME_PARSER_WSDL_MULTIPLEBINDING");
        check(mn.contains("RUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL"), "method RUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL");
        check(mn.contains("RUNTIME_PARSER_WSDL_NOT_FOUND"), "method RUNTIME_PARSER_WSDL_NOT_FOUND");
        check(mn.contains("RUNTIME_PARSER_XML_READER"), "method RUNTIME_PARSER_XML_READER");
        check(mn.contains("RUNTIME_SAXPARSER_EXCEPTION"), "method RUNTIME_SAXPARSER_EXCEPTION");
        check(mn.contains("RUNTIME_WSDL_PATCHER"), "method RUNTIME_WSDL_PATCHER");
        check(mn.contains("SERVER_RT_ERR"), "method SERVER_RT_ERR");
        check(mn.contains("SERVICE_NAME_REQUIRED"), "method SERVICE_NAME_REQUIRED");
        check(mn.contains("SOAPDECODER_ERR"), "method SOAPDECODER_ERR");
        check(mn.contains("SOAPENCODER_ERR"), "method SOAPENCODER_ERR");
        check(mn.contains("STATEFUL_COOKIE_HEADER_INCORRECT"), "method STATEFUL_COOKIE_HEADER_INCORRECT");
        check(mn.contains("STATEFUL_COOKIE_HEADER_REQUIRED"), "method STATEFUL_COOKIE_HEADER_REQUIRED");
        check(mn.contains("STATEFUL_INVALID_WEBSERVICE_CONTEXT"), "method STATEFUL_INVALID_WEBSERVICE_CONTEXT");
        check(mn.contains("STATEFUL_REQURES_ADDRESSING"), "method STATEFUL_REQURES_ADDRESSING");
        check(mn.contains("STATIC_RESOURCE_INJECTION_ONLY"), "method STATIC_RESOURCE_INJECTION_ONLY");
        check(mn.contains("UNSUPPORTED_CHARSET"), "method UNSUPPORTED_CHARSET");
        check(mn.contains("UNSUPPORTED_CONTENT_TYPE"), "method UNSUPPORTED_CONTENT_TYPE");
        check(mn.contains("WRONG_FIELD_TYPE"), "method WRONG_FIELD_TYPE");
        check(mn.contains("WRONG_NO_PARAMETERS"), "method WRONG_NO_PARAMETERS");
        check(mn.contains("WRONG_PARAMETER_TYPE"), "method WRONG_PARAMETER_TYPE");
        check(mn.contains("WRONG_TNS_FOR_PORT"), "method WRONG_TNS_FOR_PORT");
        check(mn.contains("WSDL_REQUIRED"), "method WSDL_REQUIRED");
        check(mn.contains("localizableALREADY_HTTPS_SERVER"), "method localizableALREADY_HTTPS_SERVER");
        check(mn.contains("localizableALREADY_HTTP_SERVER"), "method localizableALREADY_HTTP_SERVER");
        check(mn.contains("localizableANNOTATION_ONLY_ONCE"), "method localizableANNOTATION_ONLY_ONCE");
        check(mn.contains("localizableCAN_NOT_GENERATE_WSDL"), "method localizableCAN_NOT_GENERATE_WSDL");
        check(mn.contains("localizableDD_MTOM_CONFLICT"), "method localizableDD_MTOM_CONFLICT");
        check(mn.contains("localizableDISPATCH_CANNOT_FIND_METHOD"), "method localizableDISPATCH_CANNOT_FIND_METHOD");
        check(mn.contains("localizableDUPLICATE_ABSTRACT_WSDL"), "method localizableDUPLICATE_ABSTRACT_WSDL");
        check(mn.contains("localizableDUPLICATE_PORT_KNOWN_HEADER"), "method localizableDUPLICATE_PORT_KNOWN_HEADER");
        check(mn.contains("localizableDUPLICATE_PRIMARY_WSDL"), "method localizableDUPLICATE_PRIMARY_WSDL");
        check(mn.contains("localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER"), "method localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER");
        check(mn.contains("localizableGENERATE_NON_STANDARD_WSDL"), "method localizableGENERATE_NON_STANDARD_WSDL");
        check(mn.contains("localizableNON_UNIQUE_DISPATCH_QNAME"), "method localizableNON_UNIQUE_DISPATCH_QNAME");
        check(mn.contains("localizableNOT_IMPLEMENT_PROVIDER"), "method localizableNOT_IMPLEMENT_PROVIDER");
        check(mn.contains("localizableNOT_KNOW_HTTP_CONTEXT_TYPE"), "method localizableNOT_KNOW_HTTP_CONTEXT_TYPE");
        check(mn.contains("localizableNOT_ZERO_PARAMETERS"), "method localizableNOT_ZERO_PARAMETERS");
        check(mn.contains("localizableNO_CONTENT_TYPE"), "method localizableNO_CONTENT_TYPE");
        check(mn.contains("localizableNO_CURRENT_PACKET"), "method localizableNO_CURRENT_PACKET");
        check(mn.contains("localizableNULL_IMPLEMENTOR"), "method localizableNULL_IMPLEMENTOR");
        check(mn.contains("localizablePORT_NAME_REQUIRED"), "method localizablePORT_NAME_REQUIRED");
        check(mn.contains("localizablePROVIDER_INVALID_PARAMETER_TYPE"), "method localizablePROVIDER_INVALID_PARAMETER_TYPE");
        check(mn.contains("localizablePROVIDER_NOT_PARAMETERIZED"), "method localizablePROVIDER_NOT_PARAMETERIZED");
        check(mn.contains("localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL"), "method localizableRUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL");
        check(mn.contains("localizableRUNTIME_PARSER_CLASS_NOT_FOUND"), "method localizableRUNTIME_PARSER_CLASS_NOT_FOUND");
        check(mn.contains("localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE"), "method localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE");
        check(mn.contains("localizableRUNTIME_PARSER_INVALID_ELEMENT"), "method localizableRUNTIME_PARSER_INVALID_ELEMENT");
        check(mn.contains("localizableRUNTIME_PARSER_INVALID_VERSION_NUMBER"), "method localizableRUNTIME_PARSER_INVALID_VERSION_NUMBER");
        check(mn.contains("localizableRUNTIME_PARSER_MISSING_ATTRIBUTE"), "method localizableRUNTIME_PARSER_MISSING_ATTRIBUTE");
        check(mn.contains("localizableRUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE"), "method localizableRUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE");
        check(mn.contains("localizableRUNTIME_PARSER_UNEXPECTED_CONTENT"), "method localizableRUNTIME_PARSER_UNEXPECTED_CONTENT");
        check(mn.contains("localizableRUNTIME_PARSER_WRONG_ELEMENT"), "method localizableRUNTIME_PARSER_WRONG_ELEMENT");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL"), "method localizableRUNTIME_PARSER_WSDL");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICE"), "method localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICE");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT"), "method localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING"), "method localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL"), "method localizableRUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL");
        check(mn.contains("localizableRUNTIME_PARSER_WSDL_NOT_FOUND"), "method localizableRUNTIME_PARSER_WSDL_NOT_FOUND");
        check(mn.contains("localizableRUNTIME_PARSER_XML_READER"), "method localizableRUNTIME_PARSER_XML_READER");
        check(mn.contains("localizableRUNTIME_SAXPARSER_EXCEPTION"), "method localizableRUNTIME_SAXPARSER_EXCEPTION");
        check(mn.contains("localizableRUNTIME_WSDL_PATCHER"), "method localizableRUNTIME_WSDL_PATCHER");
        check(mn.contains("localizableSERVER_RT_ERR"), "method localizableSERVER_RT_ERR");
        check(mn.contains("localizableSERVICE_NAME_REQUIRED"), "method localizableSERVICE_NAME_REQUIRED");
        check(mn.contains("localizableSOAPDECODER_ERR"), "method localizableSOAPDECODER_ERR");
        check(mn.contains("localizableSOAPENCODER_ERR"), "method localizableSOAPENCODER_ERR");
        check(mn.contains("localizableSTATEFUL_COOKIE_HEADER_INCORRECT"), "method localizableSTATEFUL_COOKIE_HEADER_INCORRECT");
        check(mn.contains("localizableSTATEFUL_COOKIE_HEADER_REQUIRED"), "method localizableSTATEFUL_COOKIE_HEADER_REQUIRED");
        check(mn.contains("localizableSTATEFUL_INVALID_WEBSERVICE_CONTEXT"), "method localizableSTATEFUL_INVALID_WEBSERVICE_CONTEXT");
        check(mn.contains("localizableSTATEFUL_REQURES_ADDRESSING"), "method localizableSTATEFUL_REQURES_ADDRESSING");
        check(mn.contains("localizableSTATIC_RESOURCE_INJECTION_ONLY"), "method localizableSTATIC_RESOURCE_INJECTION_ONLY");
        check(mn.contains("localizableUNSUPPORTED_CHARSET"), "method localizableUNSUPPORTED_CHARSET");
        check(mn.contains("localizableUNSUPPORTED_CONTENT_TYPE"), "method localizableUNSUPPORTED_CONTENT_TYPE");
        check(mn.contains("localizableWRONG_FIELD_TYPE"), "method localizableWRONG_FIELD_TYPE");
        check(mn.contains("localizableWRONG_NO_PARAMETERS"), "method localizableWRONG_NO_PARAMETERS");
        check(mn.contains("localizableWRONG_PARAMETER_TYPE"), "method localizableWRONG_PARAMETER_TYPE");
        check(mn.contains("localizableWRONG_TNS_FOR_PORT"), "method localizableWRONG_TNS_FOR_PORT");
        check(mn.contains("localizableWSDL_REQUIRED"), "method localizableWSDL_REQUIRED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
