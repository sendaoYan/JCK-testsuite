/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.readers.SAXParserHandler conformance: class hierarchy and members.
 * @run main SAXParserHandlerTest
 */
public class SAXParserHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.readers.SAXParserHandler", false, SAXParserHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
