/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.XmlmessageMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XmlmessageMessagesTest
 */

import java.lang.reflect.Modifier;


public class XmlmessageMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.XmlmessageMessages", false, XmlmessageMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.XmlmessageMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("XML_CANNOT_INTERNALIZE_MESSAGE"), "method XML_CANNOT_INTERNALIZE_MESSAGE");
        check(mn.contains("XML_CONTENT_TYPE_MUSTBE_MULTIPART"), "method XML_CONTENT_TYPE_MUSTBE_MULTIPART");
        check(mn.contains("XML_CONTENT_TYPE_PARSE_ERR"), "method XML_CONTENT_TYPE_PARSE_ERR");
        check(mn.contains("XML_GET_DS_ERR"), "method XML_GET_DS_ERR");
        check(mn.contains("XML_GET_SOURCE_ERR"), "method XML_GET_SOURCE_ERR");
        check(mn.contains("XML_INVALID_CONTENT_TYPE"), "method XML_INVALID_CONTENT_TYPE");
        check(mn.contains("XML_NO_CONTENT_TYPE"), "method XML_NO_CONTENT_TYPE");
        check(mn.contains("XML_NULL_HEADERS"), "method XML_NULL_HEADERS");
        check(mn.contains("XML_ROOT_PART_INVALID_CONTENT_TYPE"), "method XML_ROOT_PART_INVALID_CONTENT_TYPE");
        check(mn.contains("XML_SET_PAYLOAD_ERR"), "method XML_SET_PAYLOAD_ERR");
        check(mn.contains("XML_UNKNOWN_CONTENT_TYPE"), "method XML_UNKNOWN_CONTENT_TYPE");
        check(mn.contains("localizableXML_CANNOT_INTERNALIZE_MESSAGE"), "method localizableXML_CANNOT_INTERNALIZE_MESSAGE");
        check(mn.contains("localizableXML_CONTENT_TYPE_MUSTBE_MULTIPART"), "method localizableXML_CONTENT_TYPE_MUSTBE_MULTIPART");
        check(mn.contains("localizableXML_CONTENT_TYPE_PARSE_ERR"), "method localizableXML_CONTENT_TYPE_PARSE_ERR");
        check(mn.contains("localizableXML_GET_DS_ERR"), "method localizableXML_GET_DS_ERR");
        check(mn.contains("localizableXML_GET_SOURCE_ERR"), "method localizableXML_GET_SOURCE_ERR");
        check(mn.contains("localizableXML_INVALID_CONTENT_TYPE"), "method localizableXML_INVALID_CONTENT_TYPE");
        check(mn.contains("localizableXML_NO_CONTENT_TYPE"), "method localizableXML_NO_CONTENT_TYPE");
        check(mn.contains("localizableXML_NULL_HEADERS"), "method localizableXML_NULL_HEADERS");
        check(mn.contains("localizableXML_ROOT_PART_INVALID_CONTENT_TYPE"), "method localizableXML_ROOT_PART_INVALID_CONTENT_TYPE");
        check(mn.contains("localizableXML_SET_PAYLOAD_ERR"), "method localizableXML_SET_PAYLOAD_ERR");
        check(mn.contains("localizableXML_UNKNOWN_CONTENT_TYPE"), "method localizableXML_UNKNOWN_CONTENT_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
