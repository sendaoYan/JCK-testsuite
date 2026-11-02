/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.AddressingMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AddressingMessagesTest
 */

import java.lang.reflect.Modifier;


public class AddressingMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.AddressingMessages", false, AddressingMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.AddressingMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("ACTION_NOT_SUPPORTED_EXCEPTION"), "method ACTION_NOT_SUPPORTED_EXCEPTION");
        check(mn.contains("ADDRESSING_NOT_ENABLED"), "method ADDRESSING_NOT_ENABLED");
        check(mn.contains("ADDRESSING_SHOULD_BE_ENABLED"), "method ADDRESSING_SHOULD_BE_ENABLED");
        check(mn.contains("FAULT_TO_CANNOT_PARSE"), "method FAULT_TO_CANNOT_PARSE");
        check(mn.contains("INVALID_ADDRESSING_HEADER_EXCEPTION"), "method INVALID_ADDRESSING_HEADER_EXCEPTION");
        check(mn.contains("INVALID_WSAW_ANONYMOUS"), "method INVALID_WSAW_ANONYMOUS");
        check(mn.contains("MISSING_HEADER_EXCEPTION"), "method MISSING_HEADER_EXCEPTION");
        check(mn.contains("NON_ANONYMOUS_RESPONSE"), "method NON_ANONYMOUS_RESPONSE");
        check(mn.contains("NON_ANONYMOUS_RESPONSE_NULL_HEADERS"), "method NON_ANONYMOUS_RESPONSE_NULL_HEADERS");
        check(mn.contains("NON_ANONYMOUS_RESPONSE_ONEWAY"), "method NON_ANONYMOUS_RESPONSE_ONEWAY");
        check(mn.contains("NON_ANONYMOUS_RESPONSE_SENDING"), "method NON_ANONYMOUS_RESPONSE_SENDING");
        check(mn.contains("NON_ANONYMOUS_UNKNOWN_PROTOCOL"), "method NON_ANONYMOUS_UNKNOWN_PROTOCOL");
        check(mn.contains("NON_UNIQUE_OPERATION_SIGNATURE"), "method NON_UNIQUE_OPERATION_SIGNATURE");
        check(mn.contains("NULL_ACTION"), "method NULL_ACTION");
        check(mn.contains("NULL_ADDRESSING_VERSION"), "method NULL_ADDRESSING_VERSION");
        check(mn.contains("NULL_BINDING"), "method NULL_BINDING");
        check(mn.contains("NULL_HEADERS"), "method NULL_HEADERS");
        check(mn.contains("NULL_MESSAGE"), "method NULL_MESSAGE");
        check(mn.contains("NULL_PACKET"), "method NULL_PACKET");
        check(mn.contains("NULL_SOAP_VERSION"), "method NULL_SOAP_VERSION");
        check(mn.contains("NULL_WSA_HEADERS"), "method NULL_WSA_HEADERS");
        check(mn.contains("NULL_WSDL_PORT"), "method NULL_WSDL_PORT");
        check(mn.contains("REPLY_TO_CANNOT_PARSE"), "method REPLY_TO_CANNOT_PARSE");
        check(mn.contains("UNKNOWN_WSA_HEADER"), "method UNKNOWN_WSA_HEADER");
        check(mn.contains("VALIDATION_CLIENT_NULL_ACTION"), "method VALIDATION_CLIENT_NULL_ACTION");
        check(mn.contains("VALIDATION_SERVER_NULL_ACTION"), "method VALIDATION_SERVER_NULL_ACTION");
        check(mn.contains("WRONG_ADDRESSING_VERSION"), "method WRONG_ADDRESSING_VERSION");
        check(mn.contains("WSAW_ANONYMOUS_PROHIBITED"), "method WSAW_ANONYMOUS_PROHIBITED");
        check(mn.contains("WSDL_BOUND_OPERATION_NOT_FOUND"), "method WSDL_BOUND_OPERATION_NOT_FOUND");
        check(mn.contains("localizableACTION_NOT_SUPPORTED_EXCEPTION"), "method localizableACTION_NOT_SUPPORTED_EXCEPTION");
        check(mn.contains("localizableADDRESSING_NOT_ENABLED"), "method localizableADDRESSING_NOT_ENABLED");
        check(mn.contains("localizableADDRESSING_SHOULD_BE_ENABLED"), "method localizableADDRESSING_SHOULD_BE_ENABLED");
        check(mn.contains("localizableFAULT_TO_CANNOT_PARSE"), "method localizableFAULT_TO_CANNOT_PARSE");
        check(mn.contains("localizableINVALID_ADDRESSING_HEADER_EXCEPTION"), "method localizableINVALID_ADDRESSING_HEADER_EXCEPTION");
        check(mn.contains("localizableINVALID_WSAW_ANONYMOUS"), "method localizableINVALID_WSAW_ANONYMOUS");
        check(mn.contains("localizableMISSING_HEADER_EXCEPTION"), "method localizableMISSING_HEADER_EXCEPTION");
        check(mn.contains("localizableNON_ANONYMOUS_RESPONSE"), "method localizableNON_ANONYMOUS_RESPONSE");
        check(mn.contains("localizableNON_ANONYMOUS_RESPONSE_NULL_HEADERS"), "method localizableNON_ANONYMOUS_RESPONSE_NULL_HEADERS");
        check(mn.contains("localizableNON_ANONYMOUS_RESPONSE_ONEWAY"), "method localizableNON_ANONYMOUS_RESPONSE_ONEWAY");
        check(mn.contains("localizableNON_ANONYMOUS_RESPONSE_SENDING"), "method localizableNON_ANONYMOUS_RESPONSE_SENDING");
        check(mn.contains("localizableNON_ANONYMOUS_UNKNOWN_PROTOCOL"), "method localizableNON_ANONYMOUS_UNKNOWN_PROTOCOL");
        check(mn.contains("localizableNON_UNIQUE_OPERATION_SIGNATURE"), "method localizableNON_UNIQUE_OPERATION_SIGNATURE");
        check(mn.contains("localizableNULL_ACTION"), "method localizableNULL_ACTION");
        check(mn.contains("localizableNULL_ADDRESSING_VERSION"), "method localizableNULL_ADDRESSING_VERSION");
        check(mn.contains("localizableNULL_BINDING"), "method localizableNULL_BINDING");
        check(mn.contains("localizableNULL_HEADERS"), "method localizableNULL_HEADERS");
        check(mn.contains("localizableNULL_MESSAGE"), "method localizableNULL_MESSAGE");
        check(mn.contains("localizableNULL_PACKET"), "method localizableNULL_PACKET");
        check(mn.contains("localizableNULL_SOAP_VERSION"), "method localizableNULL_SOAP_VERSION");
        check(mn.contains("localizableNULL_WSA_HEADERS"), "method localizableNULL_WSA_HEADERS");
        check(mn.contains("localizableNULL_WSDL_PORT"), "method localizableNULL_WSDL_PORT");
        check(mn.contains("localizableREPLY_TO_CANNOT_PARSE"), "method localizableREPLY_TO_CANNOT_PARSE");
        check(mn.contains("localizableUNKNOWN_WSA_HEADER"), "method localizableUNKNOWN_WSA_HEADER");
        check(mn.contains("localizableVALIDATION_CLIENT_NULL_ACTION"), "method localizableVALIDATION_CLIENT_NULL_ACTION");
        check(mn.contains("localizableVALIDATION_SERVER_NULL_ACTION"), "method localizableVALIDATION_SERVER_NULL_ACTION");
        check(mn.contains("localizableWRONG_ADDRESSING_VERSION"), "method localizableWRONG_ADDRESSING_VERSION");
        check(mn.contains("localizableWSAW_ANONYMOUS_PROHIBITED"), "method localizableWSAW_ANONYMOUS_PROHIBITED");
        check(mn.contains("localizableWSDL_BOUND_OPERATION_NOT_FOUND"), "method localizableWSDL_BOUND_OPERATION_NOT_FOUND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
