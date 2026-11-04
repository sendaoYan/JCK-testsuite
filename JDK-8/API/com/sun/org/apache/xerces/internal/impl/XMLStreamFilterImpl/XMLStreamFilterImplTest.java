/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLStreamFilterImpl conformance: class hierarchy and members.
 * @run main XMLStreamFilterImplTest
 */
public class XMLStreamFilterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLStreamFilterImpl", false, XMLStreamFilterImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "javax.xml.stream.XMLStreamReader"), "has interface javax.xml.stream.XMLStreamReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setStreamFilter"), "method setStreamFilter");
        check(mn.contains("next"), "method next");
        check(mn.contains("nextTag"), "method nextTag");
        check(mn.contains("hasNext"), "method hasNext");
        check(mn.contains("close"), "method close");
        check(mn.contains("getAttributeCount"), "method getAttributeCount");
        check(mn.contains("getAttributeName"), "method getAttributeName");
        check(mn.contains("getAttributeNamespace"), "method getAttributeNamespace");
        check(mn.contains("getAttributePrefix"), "method getAttributePrefix");
        check(mn.contains("getAttributeType"), "method getAttributeType");
        check(mn.contains("getAttributeValue"), "method getAttributeValue");
        check(mn.contains("getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(mn.contains("getElementText"), "method getElementText");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getEventType"), "method getEventType");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getLocation"), "method getLocation");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("getNamespaceCount"), "method getNamespaceCount");
        check(mn.contains("getNamespacePrefix"), "method getNamespacePrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPIData"), "method getPIData");
        check(mn.contains("getPITarget"), "method getPITarget");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("getText"), "method getText");
        check(mn.contains("getTextCharacters"), "method getTextCharacters");
        check(mn.contains("getTextLength"), "method getTextLength");
        check(mn.contains("getTextStart"), "method getTextStart");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("hasName"), "method hasName");
        check(mn.contains("hasText"), "method hasText");
        check(mn.contains("isAttributeSpecified"), "method isAttributeSpecified");
        check(mn.contains("isCharacters"), "method isCharacters");
        check(mn.contains("isEndElement"), "method isEndElement");
        check(mn.contains("isStandalone"), "method isStandalone");
        check(mn.contains("isStartElement"), "method isStartElement");
        check(mn.contains("isWhiteSpace"), "method isWhiteSpace");
        check(mn.contains("require"), "method require");
        check(mn.contains("standaloneSet"), "method standaloneSet");
        check(mn.contains("getAttributeLocalName"), "method getAttributeLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
