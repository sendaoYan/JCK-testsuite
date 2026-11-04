/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser conformance: class hierarchy and members.
 * @run main AbstractDOMParserTest
 */
public class AbstractDOMParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser", false, AbstractDOMParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser"), "superclass com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDocumentClassName"), "method getDocumentClassName");
        check(mn.contains("setDocumentClassName"), "method setDocumentClassName");
        check(mn.contains("getDocument"), "method getDocument");
        check(mn.contains("dropDocumentReferences"), "method dropDocumentReferences");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("handleBaseURI"), "method handleBaseURI");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("createElementNode"), "method createElementNode");
        check(mn.contains("createAttrNode"), "method createAttrNode");
        check(mn.contains("setCharacterData"), "method setCharacterData");
        check(mn.contains("abort"), "method abort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
