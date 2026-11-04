/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.PolicyMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyMessagesTest
 */

import java.lang.reflect.Modifier;


public class PolicyMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.PolicyMessages", false, PolicyMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.PolicyMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE"), "method WSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE");
        check(mn.contains("WSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE"), "method WSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE");
        check(mn.contains("WSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME"), "method WSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME");
        check(mn.contains("WSP_1004_POLICY_URIS_CAN_NOT_BE_NULL"), "method WSP_1004_POLICY_URIS_CAN_NOT_BE_NULL");
        check(mn.contains("WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST"), "method WSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST");
        check(mn.contains("WSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL"), "method WSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL");
        check(mn.contains("WSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL"), "method WSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL");
        check(mn.contains("WSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL"), "method WSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL");
        check(mn.contains("WSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL"), "method WSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL");
        check(mn.contains("WSP_1010_NO_POLICIES_DEFINED"), "method WSP_1010_NO_POLICIES_DEFINED");
        check(mn.contains("WSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT"), "method WSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT");
        check(mn.contains("WSP_1012_FAILED_CONFIGURE_WSDL_MODEL"), "method WSP_1012_FAILED_CONFIGURE_WSDL_MODEL");
        check(mn.contains("WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT"), "method WSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT");
        check(mn.contains("WSP_1014_CAN_NOT_FIND_POLICY"), "method WSP_1014_CAN_NOT_FIND_POLICY");
        check(mn.contains("WSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED"), "method WSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED");
        check(mn.contains("WSP_1016_POLICY_ID_NULL_OR_DUPLICATE"), "method WSP_1016_POLICY_ID_NULL_OR_DUPLICATE");
        check(mn.contains("WSP_1017_MAP_UPDATE_FAILED"), "method WSP_1017_MAP_UPDATE_FAILED");
        check(mn.contains("WSP_1018_FAILED_TO_MARSHALL_POLICY"), "method WSP_1018_FAILED_TO_MARSHALL_POLICY");
        check(mn.contains("WSP_1019_CREATE_EMPTY_POLICY_MAP"), "method WSP_1019_CREATE_EMPTY_POLICY_MAP");
        check(mn.contains("WSP_1020_DUPLICATE_ID"), "method WSP_1020_DUPLICATE_ID");
        check(mn.contains("WSP_1021_FAULT_NOT_BOUND"), "method WSP_1021_FAULT_NOT_BOUND");
        check(mn.contains("localizableWSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE"), "method localizableWSP_1001_XML_EXCEPTION_WHEN_PROCESSING_POLICY_REFERENCE");
        check(mn.contains("localizableWSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE"), "method localizableWSP_1002_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE");
        check(mn.contains("localizableWSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME"), "method localizableWSP_1003_UNABLE_TO_CHECK_ELEMENT_NAME");
        check(mn.contains("localizableWSP_1004_POLICY_URIS_CAN_NOT_BE_NULL"), "method localizableWSP_1004_POLICY_URIS_CAN_NOT_BE_NULL");
        check(mn.contains("localizableWSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST"), "method localizableWSP_1005_POLICY_REFERENCE_DOES_NOT_EXIST");
        check(mn.contains("localizableWSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL"), "method localizableWSP_1006_POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL");
        check(mn.contains("localizableWSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL"), "method localizableWSP_1007_POLICY_EXCEPTION_WHILE_FINISHING_PARSING_WSDL");
        check(mn.contains("localizableWSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL"), "method localizableWSP_1008_NOT_MARSHALLING_WSDL_SUBJ_NULL");
        check(mn.contains("localizableWSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL"), "method localizableWSP_1009_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL");
        check(mn.contains("localizableWSP_1010_NO_POLICIES_DEFINED"), "method localizableWSP_1010_NO_POLICIES_DEFINED");
        check(mn.contains("localizableWSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT"), "method localizableWSP_1011_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT");
        check(mn.contains("localizableWSP_1012_FAILED_CONFIGURE_WSDL_MODEL"), "method localizableWSP_1012_FAILED_CONFIGURE_WSDL_MODEL");
        check(mn.contains("localizableWSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT"), "method localizableWSP_1013_EXCEPTION_WHEN_READING_POLICY_ELEMENT");
        check(mn.contains("localizableWSP_1014_CAN_NOT_FIND_POLICY"), "method localizableWSP_1014_CAN_NOT_FIND_POLICY");
        check(mn.contains("localizableWSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED"), "method localizableWSP_1015_SERVER_SIDE_ASSERTION_VALIDATION_FAILED");
        check(mn.contains("localizableWSP_1016_POLICY_ID_NULL_OR_DUPLICATE"), "method localizableWSP_1016_POLICY_ID_NULL_OR_DUPLICATE");
        check(mn.contains("localizableWSP_1017_MAP_UPDATE_FAILED"), "method localizableWSP_1017_MAP_UPDATE_FAILED");
        check(mn.contains("localizableWSP_1018_FAILED_TO_MARSHALL_POLICY"), "method localizableWSP_1018_FAILED_TO_MARSHALL_POLICY");
        check(mn.contains("localizableWSP_1019_CREATE_EMPTY_POLICY_MAP"), "method localizableWSP_1019_CREATE_EMPTY_POLICY_MAP");
        check(mn.contains("localizableWSP_1020_DUPLICATE_ID"), "method localizableWSP_1020_DUPLICATE_ID");
        check(mn.contains("localizableWSP_1021_FAULT_NOT_BOUND"), "method localizableWSP_1021_FAULT_NOT_BOUND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
