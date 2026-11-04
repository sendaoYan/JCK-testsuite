/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.util.StAXParserWrapper conformance: class hierarchy and members.
 * @run main StAXParserWrapperTest
 */

public class StAXParserWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.util.StAXParserWrapper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.util.StAXParserWrapper"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.XMLStreamReader"), "has interface javax.xml.stream.XMLStreamReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("getAttributeCount"), "has method getAttributeCount");
        check(mn.contains("getAttributeLocalName"), "has method getAttributeLocalName");
        check(mn.contains("getAttributeName"), "has method getAttributeName");
        check(mn.contains("getAttributeNamespace"), "has method getAttributeNamespace");
        check(mn.contains("getAttributePrefix"), "has method getAttributePrefix");
        check(mn.contains("getAttributeType"), "has method getAttributeType");
        check(mn.contains("getAttributeValue"), "has method getAttributeValue");
        check(mn.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(mn.contains("getElementText"), "has method getElementText");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("getEventType"), "has method getEventType");
        check(mn.contains("getLocalName"), "has method getLocalName");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getNamespaceCount"), "has method getNamespaceCount");
        check(mn.contains("getNamespacePrefix"), "has method getNamespacePrefix");
        check(mn.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(mn.contains("getPIData"), "has method getPIData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
