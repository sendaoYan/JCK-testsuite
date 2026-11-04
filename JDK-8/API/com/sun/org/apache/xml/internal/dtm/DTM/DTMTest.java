/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.DTM conformance: interface hierarchy and members.
 * @run main DTMTest
 */
public class DTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.DTM", false, DTMTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getAxisTraverser"), "method getAxisTraverser");
        check(mn.contains("getAxisIterator"), "method getAxisIterator");
        check(mn.contains("getTypedAxisIterator"), "method getTypedAxisIterator");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getFirstAttribute"), "method getFirstAttribute");
        check(mn.contains("getFirstNamespaceNode"), "method getFirstNamespaceNode");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextAttribute"), "method getNextAttribute");
        check(mn.contains("getNextNamespaceNode"), "method getNextNamespaceNode");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("getDocument"), "method getDocument");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("getDocumentRoot"), "method getDocumentRoot");
        check(mn.contains("getStringValue"), "method getStringValue");
        check(mn.contains("getStringValueChunkCount"), "method getStringValueChunkCount");
        check(mn.contains("getStringValueChunk"), "method getStringValueChunk");
        check(mn.contains("getExpandedTypeID"), "method getExpandedTypeID");
        check(mn.contains("getLocalNameFromExpandedNameID"), "method getLocalNameFromExpandedNameID");
        check(mn.contains("getNamespaceFromExpandedNameID"), "method getNamespaceFromExpandedNameID");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeNameX"), "method getNodeNameX");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getLevel"), "method getLevel");
        check(mn.contains("isSupported"), "method isSupported");
        check(mn.contains("getDocumentBaseURI"), "method getDocumentBaseURI");
        check(mn.contains("setDocumentBaseURI"), "method setDocumentBaseURI");
        check(mn.contains("getDocumentSystemIdentifier"), "method getDocumentSystemIdentifier");
        check(mn.contains("getDocumentEncoding"), "method getDocumentEncoding");
        check(mn.contains("getDocumentStandalone"), "method getDocumentStandalone");
        check(mn.contains("getDocumentVersion"), "method getDocumentVersion");
        check(mn.contains("getDocumentAllDeclarationsProcessed"), "method getDocumentAllDeclarationsProcessed");
        check(mn.contains("getDocumentTypeDeclarationSystemIdentifier"), "method getDocumentTypeDeclarationSystemIdentifier");
        check(mn.contains("getDocumentTypeDeclarationPublicIdentifier"), "method getDocumentTypeDeclarationPublicIdentifier");
        check(mn.contains("getElementById"), "method getElementById");
        check(mn.contains("getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(mn.contains("supportsPreStripping"), "method supportsPreStripping");
        check(mn.contains("isNodeAfter"), "method isNodeAfter");
        check(mn.contains("isCharacterElementContentWhitespace"), "method isCharacterElementContentWhitespace");
        check(mn.contains("isDocumentAllDeclarationsProcessed"), "method isDocumentAllDeclarationsProcessed");
        check(mn.contains("isAttributeSpecified"), "method isAttributeSpecified");
        check(mn.contains("dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(mn.contains("dispatchToEvents"), "method dispatchToEvents");
        check(mn.contains("getNode"), "method getNode");
        check(mn.contains("needsTwoThreads"), "method needsTwoThreads");
        check(mn.contains("getContentHandler"), "method getContentHandler");
        check(mn.contains("getLexicalHandler"), "method getLexicalHandler");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("getDeclHandler"), "method getDeclHandler");
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("appendTextChild"), "method appendTextChild");
        check(mn.contains("getSourceLocatorFor"), "method getSourceLocatorFor");
        check(mn.contains("documentRegistration"), "method documentRegistration");
        check(mn.contains("documentRelease"), "method documentRelease");
        check(mn.contains("migrateTo"), "method migrateTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
