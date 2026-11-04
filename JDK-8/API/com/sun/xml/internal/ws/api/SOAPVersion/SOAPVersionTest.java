/*
 * @test
 * @summary com.sun.xml.internal.ws.api.SOAPVersion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPVersionTest
 */

import java.lang.reflect.Modifier;


public class SOAPVersionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.SOAPVersion", false, SOAPVersionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.SOAPVersion"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("from"), "method from");
        check(mn.contains("fromHttpBinding"), "method fromHttpBinding");
        check(mn.contains("fromNsUri"), "method fromNsUri");
        check(mn.contains("getMessageFactory"), "method getMessageFactory");
        check(mn.contains("getSOAPFactory"), "method getSOAPFactory");
        check(mn.contains("toFeature"), "method toFeature");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("SOAP_11"), "field SOAP_11");
        check(fn.contains("SOAP_12"), "field SOAP_12");
        check(fn.contains("contentType"), "field contentType");
        check(fn.contains("faultCodeClient"), "field faultCodeClient");
        check(fn.contains("faultCodeMustUnderstand"), "field faultCodeMustUnderstand");
        check(fn.contains("faultCodeServer"), "field faultCodeServer");
        check(fn.contains("httpBindingId"), "field httpBindingId");
        check(fn.contains("implicitRole"), "field implicitRole");
        check(fn.contains("implicitRoleSet"), "field implicitRoleSet");
        check(fn.contains("nsUri"), "field nsUri");
        check(fn.contains("requiredRoles"), "field requiredRoles");
        check(fn.contains("roleAttributeName"), "field roleAttributeName");
        check(fn.contains("saajMessageFactory"), "field saajMessageFactory");
        check(fn.contains("saajSoapFactory"), "field saajSoapFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
