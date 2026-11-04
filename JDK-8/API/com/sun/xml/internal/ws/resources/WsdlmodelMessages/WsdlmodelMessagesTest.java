/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.WsdlmodelMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WsdlmodelMessagesTest
 */

import java.lang.reflect.Modifier;


public class WsdlmodelMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.WsdlmodelMessages", false, WsdlmodelMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.WsdlmodelMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("MEX_METADATA_SYSTEMID_NULL"), "method MEX_METADATA_SYSTEMID_NULL");
        check(mn.contains("WSDL_IMPORT_SHOULD_BE_WSDL"), "method WSDL_IMPORT_SHOULD_BE_WSDL");
        check(mn.contains("WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH"), "method WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH");
        check(mn.contains("localizableMEX_METADATA_SYSTEMID_NULL"), "method localizableMEX_METADATA_SYSTEMID_NULL");
        check(mn.contains("localizableWSDL_IMPORT_SHOULD_BE_WSDL"), "method localizableWSDL_IMPORT_SHOULD_BE_WSDL");
        check(mn.contains("localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH"), "method localizableWSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
