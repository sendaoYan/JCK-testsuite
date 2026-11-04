/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.ModelerMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ModelerMessagesTest
 */

import java.lang.reflect.Modifier;


public class ModelerMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.ModelerMessages", false, ModelerMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.ModelerMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("NESTED_MODELER_ERROR"), "method NESTED_MODELER_ERROR");
        check(mn.contains("NOT_A_VALID_BARE_METHOD"), "method NOT_A_VALID_BARE_METHOD");
        check(mn.contains("RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD"), "method RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD");
        check(mn.contains("RUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD"), "method RUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD");
        check(mn.contains("RUNTIME_MODELER_CANNOT_GET_SERVICE_NAME_FROM_INTERFACE"), "method RUNTIME_MODELER_CANNOT_GET_SERVICE_NAME_FROM_INTERFACE");
        check(mn.contains("RUNTIME_MODELER_CLASS_NOT_FOUND"), "method RUNTIME_MODELER_CLASS_NOT_FOUND");
        check(mn.contains("RUNTIME_MODELER_ENDPOINT_INTERFACE_NO_WEBSERVICE"), "method RUNTIME_MODELER_ENDPOINT_INTERFACE_NO_WEBSERVICE");
        check(mn.contains("RUNTIME_MODELER_EXTERNAL_METADATA_GENERIC"), "method RUNTIME_MODELER_EXTERNAL_METADATA_GENERIC");
        check(mn.contains("RUNTIME_MODELER_EXTERNAL_METADATA_UNABLE_TO_READ"), "method RUNTIME_MODELER_EXTERNAL_METADATA_UNABLE_TO_READ");
        check(mn.contains("RUNTIME_MODELER_EXTERNAL_METADATA_UNSUPPORTED_SCHEMA"), "method RUNTIME_MODELER_EXTERNAL_METADATA_UNSUPPORTED_SCHEMA");
        check(mn.contains("RUNTIME_MODELER_EXTERNAL_METADATA_WRONG_FORMAT"), "method RUNTIME_MODELER_EXTERNAL_METADATA_WRONG_FORMAT");
        check(mn.contains("RUNTIME_MODELER_FEATURE_CONFLICT"), "method RUNTIME_MODELER_FEATURE_CONFLICT");
        check(mn.contains("RUNTIME_MODELER_INVALID_SOAPBINDING_PARAMETERSTYLE"), "method RUNTIME_MODELER_INVALID_SOAPBINDING_PARAMETERSTYLE");
        check(mn.contains("RUNTIME_MODELER_METHOD_NOT_FOUND"), "method RUNTIME_MODELER_METHOD_NOT_FOUND");
        check(mn.contains("RUNTIME_MODELER_MTOM_CONFLICT"), "method RUNTIME_MODELER_MTOM_CONFLICT");
        check(mn.contains("RUNTIME_MODELER_NO_OPERATIONS"), "method RUNTIME_MODELER_NO_OPERATIONS");
        check(mn.contains("RUNTIME_MODELER_NO_PACKAGE"), "method RUNTIME_MODELER_NO_PACKAGE");
        check(mn.contains("RUNTIME_MODELER_NO_WEBSERVICE_ANNOTATION"), "method RUNTIME_MODELER_NO_WEBSERVICE_ANNOTATION");
        check(mn.contains("RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS"), "method RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS");
        check(mn.contains("RUNTIME_MODELER_ONEWAY_OPERATION_NO_OUT_PARAMETERS"), "method RUNTIME_MODELER_ONEWAY_OPERATION_NO_OUT_PARAMETERS");
        check(mn.contains("RUNTIME_MODELER_PORTNAME_SERVICENAME_NAMESPACE_MISMATCH"), "method RUNTIME_MODELER_PORTNAME_SERVICENAME_NAMESPACE_MISMATCH");
        check(mn.contains("RUNTIME_MODELER_SOAPBINDING_CONFLICT"), "method RUNTIME_MODELER_SOAPBINDING_CONFLICT");
        check(mn.contains("RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATIC"), "method RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATIC");
        check(mn.contains("RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATICFINAL"), "method RUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATICFINAL");
        check(mn.contains("RUNTIME_MODELER_WEBMETHOD_MUST_BE_PUBLIC"), "method RUNTIME_MODELER_WEBMETHOD_MUST_BE_PUBLIC");
        check(mn.contains("RUNTIME_MODELER_WRAPPER_NOT_FOUND"), "method RUNTIME_MODELER_WRAPPER_NOT_FOUND");
        check(mn.contains("RUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR"), "method RUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR");
        check(mn.contains("RUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR"), "method RUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR");
        check(mn.contains("RUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR"), "method RUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR");
        check(mn.contains("UNABLE_TO_CREATE_JAXB_CONTEXT"), "method UNABLE_TO_CREATE_JAXB_CONTEXT");
        check(mn.contains("localizableNESTED_MODELER_ERROR"), "method localizableNESTED_MODELER_ERROR");
        check(mn.contains("localizableNOT_A_VALID_BARE_METHOD"), "method localizableNOT_A_VALID_BARE_METHOD");
        check(mn.contains("localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD"), "method localizableRUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD");
        check(mn.contains("localizableRUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD"), "method localizableRUNTIME_MODELER_ADDRESSING_RESPONSES_NOSUCHMETHOD");
        check(mn.contains("localizableRUNTIME_MODELER_CANNOT_GET_SERVICE_NAME_FROM_INTERFACE"), "method localizableRUNTIME_MODELER_CANNOT_GET_SERVICE_NAME_FROM_INTERFACE");
        check(mn.contains("localizableRUNTIME_MODELER_CLASS_NOT_FOUND"), "method localizableRUNTIME_MODELER_CLASS_NOT_FOUND");
        check(mn.contains("localizableRUNTIME_MODELER_ENDPOINT_INTERFACE_NO_WEBSERVICE"), "method localizableRUNTIME_MODELER_ENDPOINT_INTERFACE_NO_WEBSERVICE");
        check(mn.contains("localizableRUNTIME_MODELER_EXTERNAL_METADATA_GENERIC"), "method localizableRUNTIME_MODELER_EXTERNAL_METADATA_GENERIC");
        check(mn.contains("localizableRUNTIME_MODELER_EXTERNAL_METADATA_UNABLE_TO_READ"), "method localizableRUNTIME_MODELER_EXTERNAL_METADATA_UNABLE_TO_READ");
        check(mn.contains("localizableRUNTIME_MODELER_EXTERNAL_METADATA_UNSUPPORTED_SCHEMA"), "method localizableRUNTIME_MODELER_EXTERNAL_METADATA_UNSUPPORTED_SCHEMA");
        check(mn.contains("localizableRUNTIME_MODELER_EXTERNAL_METADATA_WRONG_FORMAT"), "method localizableRUNTIME_MODELER_EXTERNAL_METADATA_WRONG_FORMAT");
        check(mn.contains("localizableRUNTIME_MODELER_FEATURE_CONFLICT"), "method localizableRUNTIME_MODELER_FEATURE_CONFLICT");
        check(mn.contains("localizableRUNTIME_MODELER_INVALID_SOAPBINDING_PARAMETERSTYLE"), "method localizableRUNTIME_MODELER_INVALID_SOAPBINDING_PARAMETERSTYLE");
        check(mn.contains("localizableRUNTIME_MODELER_METHOD_NOT_FOUND"), "method localizableRUNTIME_MODELER_METHOD_NOT_FOUND");
        check(mn.contains("localizableRUNTIME_MODELER_MTOM_CONFLICT"), "method localizableRUNTIME_MODELER_MTOM_CONFLICT");
        check(mn.contains("localizableRUNTIME_MODELER_NO_OPERATIONS"), "method localizableRUNTIME_MODELER_NO_OPERATIONS");
        check(mn.contains("localizableRUNTIME_MODELER_NO_PACKAGE"), "method localizableRUNTIME_MODELER_NO_PACKAGE");
        check(mn.contains("localizableRUNTIME_MODELER_NO_WEBSERVICE_ANNOTATION"), "method localizableRUNTIME_MODELER_NO_WEBSERVICE_ANNOTATION");
        check(mn.contains("localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS"), "method localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS");
        check(mn.contains("localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_OUT_PARAMETERS"), "method localizableRUNTIME_MODELER_ONEWAY_OPERATION_NO_OUT_PARAMETERS");
        check(mn.contains("localizableRUNTIME_MODELER_PORTNAME_SERVICENAME_NAMESPACE_MISMATCH"), "method localizableRUNTIME_MODELER_PORTNAME_SERVICENAME_NAMESPACE_MISMATCH");
        check(mn.contains("localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT"), "method localizableRUNTIME_MODELER_SOAPBINDING_CONFLICT");
        check(mn.contains("localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATIC"), "method localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATIC");
        check(mn.contains("localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATICFINAL"), "method localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_NONSTATICFINAL");
        check(mn.contains("localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_PUBLIC"), "method localizableRUNTIME_MODELER_WEBMETHOD_MUST_BE_PUBLIC");
        check(mn.contains("localizableRUNTIME_MODELER_WRAPPER_NOT_FOUND"), "method localizableRUNTIME_MODELER_WRAPPER_NOT_FOUND");
        check(mn.contains("localizableRUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR"), "method localizableRUNTIME_MODELER_WSFEATURE_ILLEGAL_FTRCONSTRUCTOR");
        check(mn.contains("localizableRUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR"), "method localizableRUNTIME_MODELER_WSFEATURE_MORETHANONE_FTRCONSTRUCTOR");
        check(mn.contains("localizableRUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR"), "method localizableRUNTIME_MODELER_WSFEATURE_NO_FTRCONSTRUCTOR");
        check(mn.contains("localizableUNABLE_TO_CREATE_JAXB_CONTEXT"), "method localizableUNABLE_TO_CREATE_JAXB_CONTEXT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
