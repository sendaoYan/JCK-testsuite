/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.EncodingMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EncodingMessagesTest
 */

import java.lang.reflect.Modifier;


public class EncodingMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.EncodingMessages", false, EncodingMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.EncodingMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("EXCEPTION_INCORRECT_TYPE"), "method EXCEPTION_INCORRECT_TYPE");
        check(mn.contains("EXCEPTION_NOTFOUND"), "method EXCEPTION_NOTFOUND");
        check(mn.contains("FAILED_TO_READ_RESPONSE"), "method FAILED_TO_READ_RESPONSE");
        check(mn.contains("NESTED_DESERIALIZATION_ERROR"), "method NESTED_DESERIALIZATION_ERROR");
        check(mn.contains("NESTED_ENCODING_ERROR"), "method NESTED_ENCODING_ERROR");
        check(mn.contains("NESTED_SERIALIZATION_ERROR"), "method NESTED_SERIALIZATION_ERROR");
        check(mn.contains("NO_SUCH_CONTENT_ID"), "method NO_SUCH_CONTENT_ID");
        check(mn.contains("XSD_UNEXPECTED_ELEMENT_NAME"), "method XSD_UNEXPECTED_ELEMENT_NAME");
        check(mn.contains("XSD_UNKNOWN_PREFIX"), "method XSD_UNKNOWN_PREFIX");
        check(mn.contains("localizableEXCEPTION_INCORRECT_TYPE"), "method localizableEXCEPTION_INCORRECT_TYPE");
        check(mn.contains("localizableEXCEPTION_NOTFOUND"), "method localizableEXCEPTION_NOTFOUND");
        check(mn.contains("localizableFAILED_TO_READ_RESPONSE"), "method localizableFAILED_TO_READ_RESPONSE");
        check(mn.contains("localizableNESTED_DESERIALIZATION_ERROR"), "method localizableNESTED_DESERIALIZATION_ERROR");
        check(mn.contains("localizableNESTED_ENCODING_ERROR"), "method localizableNESTED_ENCODING_ERROR");
        check(mn.contains("localizableNESTED_SERIALIZATION_ERROR"), "method localizableNESTED_SERIALIZATION_ERROR");
        check(mn.contains("localizableNO_SUCH_CONTENT_ID"), "method localizableNO_SUCH_CONTENT_ID");
        check(mn.contains("localizableXSD_UNEXPECTED_ELEMENT_NAME"), "method localizableXSD_UNEXPECTED_ELEMENT_NAME");
        check(mn.contains("localizableXSD_UNKNOWN_PREFIX"), "method localizableXSD_UNKNOWN_PREFIX");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
