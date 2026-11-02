/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.ProviderApiMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ProviderApiMessagesTest
 */

import java.lang.reflect.Modifier;


public class ProviderApiMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.ProviderApiMessages", false, ProviderApiMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.ProviderApiMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("ERROR_WSDL"), "method ERROR_WSDL");
        check(mn.contains("NOTFOUND_PORT_IN_WSDL"), "method NOTFOUND_PORT_IN_WSDL");
        check(mn.contains("NOTFOUND_SERVICE_IN_WSDL"), "method NOTFOUND_SERVICE_IN_WSDL");
        check(mn.contains("NO_WSDL_NO_PORT"), "method NO_WSDL_NO_PORT");
        check(mn.contains("NULL_ADDRESS"), "method NULL_ADDRESS");
        check(mn.contains("NULL_ADDRESS_SERVICE_ENDPOINT"), "method NULL_ADDRESS_SERVICE_ENDPOINT");
        check(mn.contains("NULL_EPR"), "method NULL_EPR");
        check(mn.contains("NULL_PORTNAME"), "method NULL_PORTNAME");
        check(mn.contains("NULL_SERVICE"), "method NULL_SERVICE");
        check(mn.contains("NULL_WSDL"), "method NULL_WSDL");
        check(mn.contains("localizableERROR_WSDL"), "method localizableERROR_WSDL");
        check(mn.contains("localizableNOTFOUND_PORT_IN_WSDL"), "method localizableNOTFOUND_PORT_IN_WSDL");
        check(mn.contains("localizableNOTFOUND_SERVICE_IN_WSDL"), "method localizableNOTFOUND_SERVICE_IN_WSDL");
        check(mn.contains("localizableNO_WSDL_NO_PORT"), "method localizableNO_WSDL_NO_PORT");
        check(mn.contains("localizableNULL_ADDRESS"), "method localizableNULL_ADDRESS");
        check(mn.contains("localizableNULL_ADDRESS_SERVICE_ENDPOINT"), "method localizableNULL_ADDRESS_SERVICE_ENDPOINT");
        check(mn.contains("localizableNULL_EPR"), "method localizableNULL_EPR");
        check(mn.contains("localizableNULL_PORTNAME"), "method localizableNULL_PORTNAME");
        check(mn.contains("localizableNULL_SERVICE"), "method localizableNULL_SERVICE");
        check(mn.contains("localizableNULL_WSDL"), "method localizableNULL_WSDL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
