/*
 * @test
 * @summary com.sun.xml.internal.ws.util.xml.XmlUtil conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XmlUtilTest
 */

import java.lang.reflect.Modifier;


public class XmlUtilTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.xml.XmlUtil", false, XmlUtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.xml.XmlUtil"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("allowExternalAccess"), "method allowExternalAccess");
        check(mn.contains("createDefaultCatalogResolver"), "method createDefaultCatalogResolver");
        check(mn.contains("createEntityResolver"), "method createEntityResolver");
        check(mn.contains("getAllAttributes"), "method getAllAttributes");
        check(mn.contains("getAllChildren"), "method getAllChildren");
        check(mn.contains("getAttributeNSOrNull"), "method getAttributeNSOrNull");
        check(mn.contains("getAttributeOrNull"), "method getAttributeOrNull");
        check(mn.contains("getLocalPart"), "method getLocalPart");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getTextForNode"), "method getTextForNode");
        check(mn.contains("getUTF8Stream"), "method getUTF8Stream");
        check(mn.contains("identityTransform"), "method identityTransform");
        check(mn.contains("newDocumentBuilderFactory"), "method newDocumentBuilderFactory");
        check(mn.contains("newSAXParserFactory"), "method newSAXParserFactory");
        check(mn.contains("newTransformer"), "method newTransformer");
        check(mn.contains("newTransformerFactory"), "method newTransformerFactory");
        check(mn.contains("newXMLInputFactory"), "method newXMLInputFactory");
        check(mn.contains("newXPathFactory"), "method newXPathFactory");
        check(mn.contains("parseTokenList"), "method parseTokenList");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("DRACONIAN_ERROR_HANDLER"), "field DRACONIAN_ERROR_HANDLER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
