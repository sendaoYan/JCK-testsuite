/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.ManagementMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ManagementMessagesTest
 */

import java.lang.reflect.Modifier;


public class ManagementMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.ManagementMessages", false, ManagementMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.ManagementMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("WSM_1001_FAILED_ASSERTION"), "method WSM_1001_FAILED_ASSERTION");
        check(mn.contains("WSM_1002_EXPECTED_MANAGEMENT_ASSERTION"), "method WSM_1002_EXPECTED_MANAGEMENT_ASSERTION");
        check(mn.contains("WSM_1003_MANAGEMENT_ASSERTION_MISSING_ID"), "method WSM_1003_MANAGEMENT_ASSERTION_MISSING_ID");
        check(mn.contains("WSM_1004_EXPECTED_XML_TAG"), "method WSM_1004_EXPECTED_XML_TAG");
        check(mn.contains("WSM_1005_EXPECTED_COMMUNICATION_CHILD"), "method WSM_1005_EXPECTED_COMMUNICATION_CHILD");
        check(mn.contains("WSM_1006_CLIENT_MANAGEMENT_ENABLED"), "method WSM_1006_CLIENT_MANAGEMENT_ENABLED");
        check(mn.contains("WSM_1007_FAILED_MODEL_TRANSLATOR_INSTANTIATION"), "method WSM_1007_FAILED_MODEL_TRANSLATOR_INSTANTIATION");
        check(mn.contains("WSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE"), "method WSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE");
        check(mn.contains("localizableWSM_1001_FAILED_ASSERTION"), "method localizableWSM_1001_FAILED_ASSERTION");
        check(mn.contains("localizableWSM_1002_EXPECTED_MANAGEMENT_ASSERTION"), "method localizableWSM_1002_EXPECTED_MANAGEMENT_ASSERTION");
        check(mn.contains("localizableWSM_1003_MANAGEMENT_ASSERTION_MISSING_ID"), "method localizableWSM_1003_MANAGEMENT_ASSERTION_MISSING_ID");
        check(mn.contains("localizableWSM_1004_EXPECTED_XML_TAG"), "method localizableWSM_1004_EXPECTED_XML_TAG");
        check(mn.contains("localizableWSM_1005_EXPECTED_COMMUNICATION_CHILD"), "method localizableWSM_1005_EXPECTED_COMMUNICATION_CHILD");
        check(mn.contains("localizableWSM_1006_CLIENT_MANAGEMENT_ENABLED"), "method localizableWSM_1006_CLIENT_MANAGEMENT_ENABLED");
        check(mn.contains("localizableWSM_1007_FAILED_MODEL_TRANSLATOR_INSTANTIATION"), "method localizableWSM_1007_FAILED_MODEL_TRANSLATOR_INSTANTIATION");
        check(mn.contains("localizableWSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE"), "method localizableWSM_1008_EXPECTED_INTEGER_DISPOSE_DELAY_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
