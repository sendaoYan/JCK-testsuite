/*
 * @test
 * @summary javax.xml.stream.XMLStreamReader conformance: interface hierarchy and methods.
 * @run main XMLStreamReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.XMLStreamConstants"), "implements javax.xml.stream.XMLStreamConstants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("next"), "has method next");
        check(names.contains("require"), "has method require");
        check(names.contains("getElementText"), "has method getElementText");
        check(names.contains("nextTag"), "has method nextTag");
        check(names.contains("hasNext"), "has method hasNext");
        check(names.contains("close"), "has method close");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(names.contains("isStartElement"), "has method isStartElement");
        check(names.contains("isEndElement"), "has method isEndElement");
        check(names.contains("isCharacters"), "has method isCharacters");
        check(names.contains("isWhiteSpace"), "has method isWhiteSpace");
        check(names.contains("getAttributeValue"), "has method getAttributeValue");
        check(names.contains("getAttributeCount"), "has method getAttributeCount");
        check(names.contains("getAttributeName"), "has method getAttributeName");
        check(names.contains("getAttributeNamespace"), "has method getAttributeNamespace");
        check(names.contains("getAttributeLocalName"), "has method getAttributeLocalName");
        check(names.contains("getAttributePrefix"), "has method getAttributePrefix");
        check(names.contains("getAttributeType"), "has method getAttributeType");
        check(names.contains("isAttributeSpecified"), "has method isAttributeSpecified");
        check(names.contains("getNamespaceCount"), "has method getNamespaceCount");
        check(names.contains("getNamespacePrefix"), "has method getNamespacePrefix");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("getEventType"), "has method getEventType");
        check(names.contains("getText"), "has method getText");
        check(names.contains("getTextCharacters"), "has method getTextCharacters");
        check(names.contains("getTextStart"), "has method getTextStart");
        check(names.contains("getTextLength"), "has method getTextLength");
        check(names.contains("getEncoding"), "has method getEncoding");
        check(names.contains("hasText"), "has method hasText");
        check(names.contains("getLocation"), "has method getLocation");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getLocalName"), "has method getLocalName");
        check(names.contains("hasName"), "has method hasName");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("getVersion"), "has method getVersion");
        check(names.contains("isStandalone"), "has method isStandalone");
        check(names.contains("standaloneSet"), "has method standaloneSet");
        check(names.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(names.contains("getPITarget"), "has method getPITarget");
        check(names.contains("getPIData"), "has method getPIData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
