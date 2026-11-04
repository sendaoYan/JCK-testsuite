/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.tools.ResolvingParser conformance: class hierarchy and members.
 * @run main ResolvingParserTest
 */
public class ResolvingParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.tools.ResolvingParser", false, ResolvingParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.Parser"), "has interface org.xml.sax.Parser");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "has interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "has interface org.xml.sax.DocumentHandler");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "has interface org.xml.sax.EntityResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCatalog"), "method getCatalog");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
