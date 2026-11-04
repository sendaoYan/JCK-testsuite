/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.DispatchMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DispatchMessagesTest
 */

import java.lang.reflect.Modifier;


public class DispatchMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.DispatchMessages", false, DispatchMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.DispatchMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("DUPLICATE_PORT"), "method DUPLICATE_PORT");
        check(mn.contains("INVALID_DATASOURCE_DISPATCH_BINDING"), "method INVALID_DATASOURCE_DISPATCH_BINDING");
        check(mn.contains("INVALID_DATASOURCE_DISPATCH_MSGMODE"), "method INVALID_DATASOURCE_DISPATCH_MSGMODE");
        check(mn.contains("INVALID_NULLARG_SOAP_MSGMODE"), "method INVALID_NULLARG_SOAP_MSGMODE");
        check(mn.contains("INVALID_NULLARG_URI"), "method INVALID_NULLARG_URI");
        check(mn.contains("INVALID_NULLARG_XMLHTTP_REQUEST_METHOD"), "method INVALID_NULLARG_XMLHTTP_REQUEST_METHOD");
        check(mn.contains("INVALID_QUERY_LEADING_CHAR"), "method INVALID_QUERY_LEADING_CHAR");
        check(mn.contains("INVALID_QUERY_STRING"), "method INVALID_QUERY_STRING");
        check(mn.contains("INVALID_RESPONSE"), "method INVALID_RESPONSE");
        check(mn.contains("INVALID_RESPONSE_DESERIALIZATION"), "method INVALID_RESPONSE_DESERIALIZATION");
        check(mn.contains("INVALID_SOAPMESSAGE_DISPATCH_BINDING"), "method INVALID_SOAPMESSAGE_DISPATCH_BINDING");
        check(mn.contains("INVALID_SOAPMESSAGE_DISPATCH_MSGMODE"), "method INVALID_SOAPMESSAGE_DISPATCH_MSGMODE");
        check(mn.contains("INVALID_URI"), "method INVALID_URI");
        check(mn.contains("INVALID_URI_DECODE"), "method INVALID_URI_DECODE");
        check(mn.contains("INVALID_URI_PATH_QUERY"), "method INVALID_URI_PATH_QUERY");
        check(mn.contains("INVALID_URI_RESOLUTION"), "method INVALID_URI_RESOLUTION");
        check(mn.contains("localizableDUPLICATE_PORT"), "method localizableDUPLICATE_PORT");
        check(mn.contains("localizableINVALID_DATASOURCE_DISPATCH_BINDING"), "method localizableINVALID_DATASOURCE_DISPATCH_BINDING");
        check(mn.contains("localizableINVALID_DATASOURCE_DISPATCH_MSGMODE"), "method localizableINVALID_DATASOURCE_DISPATCH_MSGMODE");
        check(mn.contains("localizableINVALID_NULLARG_SOAP_MSGMODE"), "method localizableINVALID_NULLARG_SOAP_MSGMODE");
        check(mn.contains("localizableINVALID_NULLARG_URI"), "method localizableINVALID_NULLARG_URI");
        check(mn.contains("localizableINVALID_NULLARG_XMLHTTP_REQUEST_METHOD"), "method localizableINVALID_NULLARG_XMLHTTP_REQUEST_METHOD");
        check(mn.contains("localizableINVALID_QUERY_LEADING_CHAR"), "method localizableINVALID_QUERY_LEADING_CHAR");
        check(mn.contains("localizableINVALID_QUERY_STRING"), "method localizableINVALID_QUERY_STRING");
        check(mn.contains("localizableINVALID_RESPONSE"), "method localizableINVALID_RESPONSE");
        check(mn.contains("localizableINVALID_RESPONSE_DESERIALIZATION"), "method localizableINVALID_RESPONSE_DESERIALIZATION");
        check(mn.contains("localizableINVALID_SOAPMESSAGE_DISPATCH_BINDING"), "method localizableINVALID_SOAPMESSAGE_DISPATCH_BINDING");
        check(mn.contains("localizableINVALID_SOAPMESSAGE_DISPATCH_MSGMODE"), "method localizableINVALID_SOAPMESSAGE_DISPATCH_MSGMODE");
        check(mn.contains("localizableINVALID_URI"), "method localizableINVALID_URI");
        check(mn.contains("localizableINVALID_URI_DECODE"), "method localizableINVALID_URI_DECODE");
        check(mn.contains("localizableINVALID_URI_PATH_QUERY"), "method localizableINVALID_URI_PATH_QUERY");
        check(mn.contains("localizableINVALID_URI_RESOLUTION"), "method localizableINVALID_URI_RESOLUTION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
