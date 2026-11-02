/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionAddressingConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MemberSubmissionAddressingConstantsTest
 */

import java.lang.reflect.Modifier;


public class MemberSubmissionAddressingConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionAddressingConstants", false, MemberSubmissionAddressingConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionAddressingConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ACTION_NOT_SUPPORTED_QNAME"), "field ACTION_NOT_SUPPORTED_QNAME");
        check(fn.contains("ACTION_NOT_SUPPORTED_TEXT"), "field ACTION_NOT_SUPPORTED_TEXT");
        check(fn.contains("ANONYMOUS_EPR"), "field ANONYMOUS_EPR");
        check(fn.contains("DESTINATION_UNREACHABLE_QNAME"), "field DESTINATION_UNREACHABLE_QNAME");
        check(fn.contains("DESTINATION_UNREACHABLE_TEXT"), "field DESTINATION_UNREACHABLE_TEXT");
        check(fn.contains("ENDPOINT_UNAVAILABLE_QNAME"), "field ENDPOINT_UNAVAILABLE_QNAME");
        check(fn.contains("ENDPOINT_UNAVAILABLE_TEXT"), "field ENDPOINT_UNAVAILABLE_TEXT");
        check(fn.contains("FAULT_DETAIL_QNAME"), "field FAULT_DETAIL_QNAME");
        check(fn.contains("INVALID_MAP_QNAME"), "field INVALID_MAP_QNAME");
        check(fn.contains("INVALID_MAP_TEXT"), "field INVALID_MAP_TEXT");
        check(fn.contains("MAP_REQUIRED_QNAME"), "field MAP_REQUIRED_QNAME");
        check(fn.contains("MAP_REQUIRED_TEXT"), "field MAP_REQUIRED_TEXT");
        check(fn.contains("MEX_METADATA"), "field MEX_METADATA");
        check(fn.contains("MEX_METADATA_DIALECT_ATTRIBUTE"), "field MEX_METADATA_DIALECT_ATTRIBUTE");
        check(fn.contains("MEX_METADATA_DIALECT_VALUE"), "field MEX_METADATA_DIALECT_VALUE");
        check(fn.contains("MEX_METADATA_SECTION"), "field MEX_METADATA_SECTION");
        check(fn.contains("PROBLEM_ACTION_QNAME"), "field PROBLEM_ACTION_QNAME");
        check(fn.contains("PROBLEM_HEADER_QNAME_QNAME"), "field PROBLEM_HEADER_QNAME_QNAME");
        check(fn.contains("WSA_ACTION_QNAME"), "field WSA_ACTION_QNAME");
        check(fn.contains("WSA_ADDRESS_NAME"), "field WSA_ADDRESS_NAME");
        check(fn.contains("WSA_ADDRESS_QNAME"), "field WSA_ADDRESS_QNAME");
        check(fn.contains("WSA_ANONYMOUS_ADDRESS"), "field WSA_ANONYMOUS_ADDRESS");
        check(fn.contains("WSA_DEFAULT_FAULT_ACTION"), "field WSA_DEFAULT_FAULT_ACTION");
        check(fn.contains("WSA_EPR_NAME"), "field WSA_EPR_NAME");
        check(fn.contains("WSA_EPR_QNAME"), "field WSA_EPR_QNAME");
        check(fn.contains("WSA_NAMESPACE_NAME"), "field WSA_NAMESPACE_NAME");
        check(fn.contains("WSA_NAMESPACE_POLICY_NAME"), "field WSA_NAMESPACE_POLICY_NAME");
        check(fn.contains("WSA_NAMESPACE_WSDL_NAME"), "field WSA_NAMESPACE_WSDL_NAME");
        check(fn.contains("WSA_NONE_ADDRESS"), "field WSA_NONE_ADDRESS");
        check(fn.contains("WSA_PORTNAME_NAME"), "field WSA_PORTNAME_NAME");
        check(fn.contains("WSA_PORTTYPE_NAME"), "field WSA_PORTTYPE_NAME");
        check(fn.contains("WSA_SERVICENAME_NAME"), "field WSA_SERVICENAME_NAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
