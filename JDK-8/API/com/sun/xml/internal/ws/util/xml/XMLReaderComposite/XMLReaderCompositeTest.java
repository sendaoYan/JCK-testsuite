/*
 * @test
 * @summary com.sun.xml.internal.ws.util.xml.XMLReaderComposite conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLReaderCompositeTest
 */

import java.lang.reflect.Modifier;


public class XMLReaderCompositeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.xml.XMLReaderComposite", false, XMLReaderCompositeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.xml.XMLReaderComposite"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx"), "implements com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("getAttributeCount"), "method getAttributeCount");
        check(mn.contains("getAttributeLocalName"), "method getAttributeLocalName");
        check(mn.contains("getAttributeName"), "method getAttributeName");
        check(mn.contains("getAttributeNamespace"), "method getAttributeNamespace");
        check(mn.contains("getAttributePrefix"), "method getAttributePrefix");
        check(mn.contains("getAttributeType"), "method getAttributeType");
        check(mn.contains("getAttributeValue"), "method getAttributeValue");
        check(mn.contains("getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(mn.contains("getElementText"), "method getElementText");
        check(mn.contains("getElementTextTrim"), "method getElementTextTrim");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getEventType"), "method getEventType");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getLocation"), "method getLocation");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("getNamespaceCount"), "method getNamespaceCount");
        check(mn.contains("getNamespacePrefix"), "method getNamespacePrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPCDATA"), "method getPCDATA");
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
        check(mn.contains("hasNext"), "method hasNext");
        check(mn.contains("hasText"), "method hasText");
        check(mn.contains("isAttributeSpecified"), "method isAttributeSpecified");
        check(mn.contains("isCharacters"), "method isCharacters");
        check(mn.contains("isEndElement"), "method isEndElement");
        check(mn.contains("isStandalone"), "method isStandalone");
        check(mn.contains("isStartElement"), "method isStartElement");
        check(mn.contains("isWhiteSpace"), "method isWhiteSpace");
        check(mn.contains("next"), "method next");
        check(mn.contains("nextTag"), "method nextTag");
        check(mn.contains("require"), "method require");
        check(mn.contains("standaloneSet"), "method standaloneSet");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("children"), "field children");
        check(fn.contains("elemInfo"), "field elemInfo");
        check(fn.contains("payloadIndex"), "field payloadIndex");
        check(fn.contains("payloadReader"), "field payloadReader");
        check(fn.contains("state"), "field state");
        check(fn.contains("tagInfo"), "field tagInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
