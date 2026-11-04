/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.SoapMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SoapMessagesTest
 */

import java.lang.reflect.Modifier;


public class SoapMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.SoapMessages", false, SoapMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.SoapMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("SOAP_FACTORY_CREATE_ERR"), "method SOAP_FACTORY_CREATE_ERR");
        check(mn.contains("SOAP_FAULT_CREATE_ERR"), "method SOAP_FAULT_CREATE_ERR");
        check(mn.contains("SOAP_MSG_CREATE_ERR"), "method SOAP_MSG_CREATE_ERR");
        check(mn.contains("SOAP_MSG_FACTORY_CREATE_ERR"), "method SOAP_MSG_FACTORY_CREATE_ERR");
        check(mn.contains("SOAP_PROTOCOL_INVALID_FAULT_CODE"), "method SOAP_PROTOCOL_INVALID_FAULT_CODE");
        check(mn.contains("SOAP_VERSION_MISMATCH_ERR"), "method SOAP_VERSION_MISMATCH_ERR");
        check(mn.contains("localizableSOAP_FACTORY_CREATE_ERR"), "method localizableSOAP_FACTORY_CREATE_ERR");
        check(mn.contains("localizableSOAP_FAULT_CREATE_ERR"), "method localizableSOAP_FAULT_CREATE_ERR");
        check(mn.contains("localizableSOAP_MSG_CREATE_ERR"), "method localizableSOAP_MSG_CREATE_ERR");
        check(mn.contains("localizableSOAP_MSG_FACTORY_CREATE_ERR"), "method localizableSOAP_MSG_FACTORY_CREATE_ERR");
        check(mn.contains("localizableSOAP_PROTOCOL_INVALID_FAULT_CODE"), "method localizableSOAP_PROTOCOL_INVALID_FAULT_CODE");
        check(mn.contains("localizableSOAP_VERSION_MISMATCH_ERR"), "method localizableSOAP_VERSION_MISMATCH_ERR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
