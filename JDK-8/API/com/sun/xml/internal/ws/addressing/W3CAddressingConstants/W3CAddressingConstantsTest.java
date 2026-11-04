/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.W3CAddressingConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main W3CAddressingConstantsTest
 */

import java.lang.reflect.Modifier;


public class W3CAddressingConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.W3CAddressingConstants", false, W3CAddressingConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.W3CAddressingConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ACTION_MISMATCH"), "field ACTION_MISMATCH");
        check(fn.contains("ACTION_NOT_SUPPORTED_QNAME"), "field ACTION_NOT_SUPPORTED_QNAME");
        check(fn.contains("ACTION_NOT_SUPPORTED_TEXT"), "field ACTION_NOT_SUPPORTED_TEXT");
        check(fn.contains("ANONYMOUS_EPR"), "field ANONYMOUS_EPR");
        check(fn.contains("DESTINATION_UNREACHABLE_QNAME"), "field DESTINATION_UNREACHABLE_QNAME");
        check(fn.contains("DESTINATION_UNREACHABLE_TEXT"), "field DESTINATION_UNREACHABLE_TEXT");
        check(fn.contains("DUPLICATE_MESSAGEID"), "field DUPLICATE_MESSAGEID");
        check(fn.contains("ENDPOINT_UNAVAILABLE_QNAME"), "field ENDPOINT_UNAVAILABLE_QNAME");
        check(fn.contains("ENDPOINT_UNAVAILABLE_TEXT"), "field ENDPOINT_UNAVAILABLE_TEXT");
        check(fn.contains("FAULT_DETAIL_QNAME"), "field FAULT_DETAIL_QNAME");
        check(fn.contains("INVALID_ADDRESS_SUBCODE"), "field INVALID_ADDRESS_SUBCODE");
        check(fn.contains("INVALID_CARDINALITY"), "field INVALID_CARDINALITY");
        check(fn.contains("INVALID_EPR"), "field INVALID_EPR");
        check(fn.contains("INVALID_MAP_QNAME"), "field INVALID_MAP_QNAME");
        check(fn.contains("INVALID_MAP_TEXT"), "field INVALID_MAP_TEXT");
        check(fn.contains("MAP_REQUIRED_QNAME"), "field MAP_REQUIRED_QNAME");
        check(fn.contains("MAP_REQUIRED_TEXT"), "field MAP_REQUIRED_TEXT");
        check(fn.contains("MISSING_ADDRESS_IN_EPR"), "field MISSING_ADDRESS_IN_EPR");
        check(fn.contains("ONLY_ANONYMOUS_ADDRESS_SUPPORTED"), "field ONLY_ANONYMOUS_ADDRESS_SUPPORTED");
        check(fn.contains("ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED"), "field ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED");
        check(fn.contains("PROBLEM_ACTION_QNAME"), "field PROBLEM_ACTION_QNAME");
        check(fn.contains("PROBLEM_HEADER_QNAME_QNAME"), "field PROBLEM_HEADER_QNAME_QNAME");
        check(fn.contains("WSAW_ENDPOINTNAME_NAME"), "field WSAW_ENDPOINTNAME_NAME");
        check(fn.contains("WSAW_INTERFACENAME_NAME"), "field WSAW_INTERFACENAME_NAME");
        check(fn.contains("WSAW_SERVICENAME_NAME"), "field WSAW_SERVICENAME_NAME");
        check(fn.contains("WSAW_USING_ADDRESSING_NAME"), "field WSAW_USING_ADDRESSING_NAME");
        check(fn.contains("WSAW_USING_ADDRESSING_QNAME"), "field WSAW_USING_ADDRESSING_QNAME");
        check(fn.contains("WSA_ADDRESS_NAME"), "field WSA_ADDRESS_NAME");
        check(fn.contains("WSA_ADDRESS_QNAME"), "field WSA_ADDRESS_QNAME");
        check(fn.contains("WSA_ANONYMOUS_ADDRESS"), "field WSA_ANONYMOUS_ADDRESS");
        check(fn.contains("WSA_DEFAULT_FAULT_ACTION"), "field WSA_DEFAULT_FAULT_ACTION");
        check(fn.contains("WSA_EPR_NAME"), "field WSA_EPR_NAME");
        check(fn.contains("WSA_EPR_QNAME"), "field WSA_EPR_QNAME");
        check(fn.contains("WSA_METADATA_NAME"), "field WSA_METADATA_NAME");
        check(fn.contains("WSA_METADATA_QNAME"), "field WSA_METADATA_QNAME");
        check(fn.contains("WSA_NAMESPACE_NAME"), "field WSA_NAMESPACE_NAME");
        check(fn.contains("WSA_NAMESPACE_WSDL_NAME"), "field WSA_NAMESPACE_WSDL_NAME");
        check(fn.contains("WSA_NONE_ADDRESS"), "field WSA_NONE_ADDRESS");
        check(fn.contains("WSA_REFERENCEPARAMETERS_NAME"), "field WSA_REFERENCEPARAMETERS_NAME");
        check(fn.contains("WSA_REFERENCEPARAMETERS_QNAME"), "field WSA_REFERENCEPARAMETERS_QNAME");
        check(fn.contains("WSA_REFERENCEPROPERTIES_NAME"), "field WSA_REFERENCEPROPERTIES_NAME");
        check(fn.contains("WSA_REFERENCEPROPERTIES_QNAME"), "field WSA_REFERENCEPROPERTIES_QNAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
