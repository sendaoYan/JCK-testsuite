/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM conformance: class hierarchy and members.
 * @run main SAX2DTMTest
 */
public class SAX2DTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM", false, SAX2DTMTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "has interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "has interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "has interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setUseSourceLocation"), "method setUseSourceLocation");
        check(mn.contains("_dataOrQName"), "method _dataOrQName");
        check(mn.contains("clearCoRoutine"), "method clearCoRoutine");
        check(mn.contains("setIncrementalSAXSource"), "method setIncrementalSAXSource");
        check(mn.contains("getContentHandler"), "method getContentHandler");
        check(mn.contains("getLexicalHandler"), "method getLexicalHandler");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("getDeclHandler"), "method getDeclHandler");
        check(mn.contains("needsTwoThreads"), "method needsTwoThreads");
        check(mn.contains("dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeNameX"), "method getNodeNameX");
        check(mn.contains("isAttributeSpecified"), "method isAttributeSpecified");
        check(mn.contains("getDocumentTypeDeclarationSystemIdentifier"), "method getDocumentTypeDeclarationSystemIdentifier");
        check(mn.contains("getNextNodeIdentity"), "method getNextNodeIdentity");
        check(mn.contains("dispatchToEvents"), "method dispatchToEvents");
        check(mn.contains("getNumberOfNodes"), "method getNumberOfNodes");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("addNode"), "method addNode");
        check(mn.contains("addNewDTMID"), "method addNewDTMID");
        check(mn.contains("migrateTo"), "method migrateTo");
        check(mn.contains("setSourceLocation"), "method setSourceLocation");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getDocumentTypeDeclarationPublicIdentifier"), "method getDocumentTypeDeclarationPublicIdentifier");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getStringValue"), "method getStringValue");
        check(mn.contains("isWhitespace"), "method isWhitespace");
        check(mn.contains("getElementById"), "method getElementById");
        check(mn.contains("getIdForNamespace"), "method getIdForNamespace");
        check(mn.contains("setIDAttribute"), "method setIDAttribute");
        check(mn.contains("charactersFlush"), "method charactersFlush");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("declAlreadyDeclared"), "method declAlreadyDeclared");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getSourceLocatorFor"), "method getSourceLocatorFor");
        check(mn.contains("getFixedNames"), "method getFixedNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
