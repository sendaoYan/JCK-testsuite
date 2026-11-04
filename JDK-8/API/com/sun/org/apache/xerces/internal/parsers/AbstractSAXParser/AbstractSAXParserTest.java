/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser conformance: abstract class hierarchy and members.
 * @run main AbstractSAXParserTest
 */
public class AbstractSAXParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser", false, AbstractSAXParserTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser"), "superclass com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.PSVIProvider"), "has interface com.sun.org.apache.xerces.internal.xs.PSVIProvider");
        check(hasInterface(c, "org.xml.sax.Parser"), "has interface org.xml.sax.Parser");
        check(hasInterface(c, "org.xml.sax.XMLReader"), "has interface org.xml.sax.XMLReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("getContentHandler"), "method getContentHandler");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("setDeclHandler"), "method setDeclHandler");
        check(mn.contains("getDeclHandler"), "method getDeclHandler");
        check(mn.contains("setLexicalHandler"), "method setLexicalHandler");
        check(mn.contains("getLexicalHandler"), "method getLexicalHandler");
        check(mn.contains("startNamespaceMapping"), "method startNamespaceMapping");
        check(mn.contains("endNamespaceMapping"), "method endNamespaceMapping");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getElementPSVI"), "method getElementPSVI");
        check(mn.contains("getAttributePSVI"), "method getAttributePSVI");
        check(mn.contains("getAttributePSVIByName"), "method getAttributePSVIByName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
