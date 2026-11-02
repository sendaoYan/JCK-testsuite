/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader conformance: class hierarchy and members.
 * @run main OASISXMLCatalogReaderTest
 */
public class OASISXMLCatalogReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader", false, OASISXMLCatalogReaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader"), "superclass com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser"), "has interface com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setCatalog"), "method setCatalog");
        check(mn.contains("getCatalog"), "method getCatalog");
        check(mn.contains("inExtensionNamespace"), "method inExtensionNamespace");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("checkAttributes"), "method checkAttributes");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
