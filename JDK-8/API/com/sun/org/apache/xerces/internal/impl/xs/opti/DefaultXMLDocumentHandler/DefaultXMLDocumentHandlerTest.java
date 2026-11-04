/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler conformance: class hierarchy and members.
 * @run main DefaultXMLDocumentHandlerTest
 */
public class DefaultXMLDocumentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler", false, DefaultXMLDocumentHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("any"), "method any");
        check(mn.contains("empty"), "method empty");
        check(mn.contains("startGroup"), "method startGroup");
        check(mn.contains("pcdata"), "method pcdata");
        check(mn.contains("element"), "method element");
        check(mn.contains("separator"), "method separator");
        check(mn.contains("occurrence"), "method occurrence");
        check(mn.contains("endGroup"), "method endGroup");
        check(mn.contains("endContentModel"), "method endContentModel");
        check(mn.contains("setDocumentSource"), "method setDocumentSource");
        check(mn.contains("getDocumentSource"), "method getDocumentSource");
        check(mn.contains("setDTDSource"), "method setDTDSource");
        check(mn.contains("getDTDSource"), "method getDTDSource");
        check(mn.contains("setDTDContentModelSource"), "method setDTDContentModelSource");
        check(mn.contains("getDTDContentModelSource"), "method getDTDContentModelSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
