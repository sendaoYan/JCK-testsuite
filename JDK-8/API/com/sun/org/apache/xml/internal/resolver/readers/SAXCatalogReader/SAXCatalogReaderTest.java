/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader conformance: class hierarchy and members.
 * @run main SAXCatalogReaderTest
 */
public class SAXCatalogReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader", false, SAXCatalogReaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.resolver.readers.CatalogReader"), "has interface com.sun.org.apache.xml.internal.resolver.readers.CatalogReader");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "has interface org.xml.sax.DocumentHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setParserFactory"), "method setParserFactory");
        check(mn.contains("setParserClass"), "method setParserClass");
        check(mn.contains("getParserFactory"), "method getParserFactory");
        check(mn.contains("getParserClass"), "method getParserClass");
        check(mn.contains("setCatalogParser"), "method setCatalogParser");
        check(mn.contains("getCatalogParser"), "method getCatalogParser");
        check(mn.contains("readCatalog"), "method readCatalog");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
