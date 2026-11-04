/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl conformance: reflection.
 * @run main SimpleResultTreeImplTest
 */

public class SimpleResultTreeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl", false, SimpleResultTreeImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SimpleResultTreeImpl"), "simple name SimpleResultTreeImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.EmptySerializer"), "superclass com.sun.org.apache.xml.internal.serializer.EmptySerializer");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.DOM"), "interface com.sun.org.apache.xalan.internal.xsltc.DOM");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTM"), "interface com.sun.org.apache.xml.internal.dtm.DTM");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "getDocument"), "method getDocument");
        check(has(c, "getStringValue"), "method getStringValue");
        check(has(c, "getIterator"), "method getIterator");
        check(has(c, "getChildren"), "method getChildren");
        check(has(c, "getTypedChildren"), "method getTypedChildren");
        check(has(c, "getAxisIterator"), "method getAxisIterator");
        check(has(c, "getTypedAxisIterator"), "method getTypedAxisIterator");
        check(has(c, "getNthDescendant"), "method getNthDescendant");
        check(has(c, "getNamespaceAxisIterator"), "method getNamespaceAxisIterator");
        check(has(c, "getNodeValueIterator"), "method getNodeValueIterator");
        check(has(c, "orderNodes"), "method orderNodes");
        check(has(c, "getNodeName"), "method getNodeName");
        check(has(c, "getNodeNameX"), "method getNodeNameX");
        check(has(c, "getNamespaceName"), "method getNamespaceName");
        check(has(c, "getExpandedTypeID"), "method getExpandedTypeID");
        check(has(c, "getNamespaceType"), "method getNamespaceType");
        check(has(c, "getParent"), "method getParent");
        check(has(c, "getAttributeNode"), "method getAttributeNode");
        check(has(c, "getStringValueX"), "method getStringValueX");
        check(has(c, "copy"), "method copy");
        check(has(c, "shallowCopy"), "method shallowCopy");
        check(has(c, "lessThan"), "method lessThan");
        check(has(c, "characters"), "method characters");
        check(has(c, "makeNode"), "method makeNode");
        check(has(c, "makeNodeList"), "method makeNodeList");
        check(has(c, "getLanguage"), "method getLanguage");
        check(has(c, "getSize"), "method getSize");
        check(has(c, "getDocumentURI"), "method getDocumentURI");
        check(has(c, "setFilter"), "method setFilter");
        check(has(c, "setupMapping"), "method setupMapping");
        check(has(c, "isElement"), "method isElement");
        check(has(c, "isAttribute"), "method isAttribute");
        check(has(c, "lookupNamespace"), "method lookupNamespace");
        check(has(c, "getNodeIdent"), "method getNodeIdent");
        check(has(c, "getNodeHandle"), "method getNodeHandle");
        check(has(c, "getResultTreeFrag"), "method getResultTreeFrag");
        check(has(c, "getOutputDomBuilder"), "method getOutputDomBuilder");
        check(has(c, "getNSType"), "method getNSType");
        check(has(c, "getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(has(c, "getElementsWithIDs"), "method getElementsWithIDs");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "setEscaping"), "method setEscaping");
        check(has(c, "setFeature"), "method setFeature");
        check(has(c, "setProperty"), "method setProperty");
        check(has(c, "getAxisTraverser"), "method getAxisTraverser");
        check(has(c, "hasChildNodes"), "method hasChildNodes");
        check(has(c, "getFirstChild"), "method getFirstChild");
        check(has(c, "getLastChild"), "method getLastChild");
        check(has(c, "getFirstAttribute"), "method getFirstAttribute");
        check(has(c, "getFirstNamespaceNode"), "method getFirstNamespaceNode");
        check(has(c, "getNextSibling"), "method getNextSibling");
        check(has(c, "getPreviousSibling"), "method getPreviousSibling");
        check(has(c, "getNextAttribute"), "method getNextAttribute");
        check(has(c, "getNextNamespaceNode"), "method getNextNamespaceNode");
        check(has(c, "getOwnerDocument"), "method getOwnerDocument");
        check(has(c, "getDocumentRoot"), "method getDocumentRoot");
        check(has(c, "getStringValueChunkCount"), "method getStringValueChunkCount");
        check(has(c, "getStringValueChunk"), "method getStringValueChunk");
        check(has(c, "getLocalNameFromExpandedNameID"), "method getLocalNameFromExpandedNameID");
        check(has(c, "getNamespaceFromExpandedNameID"), "method getNamespaceFromExpandedNameID");
        check(has(c, "getLocalName"), "method getLocalName");
        check(has(c, "getPrefix"), "method getPrefix");
        check(has(c, "getNamespaceURI"), "method getNamespaceURI");
        check(has(c, "getNodeValue"), "method getNodeValue");
        check(has(c, "getNodeType"), "method getNodeType");
        check(has(c, "getLevel"), "method getLevel");
        check(has(c, "isSupported"), "method isSupported");
        check(has(c, "getDocumentBaseURI"), "method getDocumentBaseURI");
        check(has(c, "setDocumentBaseURI"), "method setDocumentBaseURI");
        check(has(c, "getDocumentSystemIdentifier"), "method getDocumentSystemIdentifier");
        check(has(c, "getDocumentEncoding"), "method getDocumentEncoding");
        check(has(c, "getDocumentStandalone"), "method getDocumentStandalone");
        check(has(c, "getDocumentVersion"), "method getDocumentVersion");
        check(has(c, "getDocumentAllDeclarationsProcessed"), "method getDocumentAllDeclarationsProcessed");
        check(has(c, "getDocumentTypeDeclarationSystemIdentifier"), "method getDocumentTypeDeclarationSystemIdentifier");
        check(has(c, "getDocumentTypeDeclarationPublicIdentifier"), "method getDocumentTypeDeclarationPublicIdentifier");
        check(has(c, "getElementById"), "method getElementById");
        check(has(c, "supportsPreStripping"), "method supportsPreStripping");
        check(has(c, "isNodeAfter"), "method isNodeAfter");
        check(has(c, "isCharacterElementContentWhitespace"), "method isCharacterElementContentWhitespace");
        check(has(c, "isDocumentAllDeclarationsProcessed"), "method isDocumentAllDeclarationsProcessed");
        check(has(c, "isAttributeSpecified"), "method isAttributeSpecified");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "dispatchToEvents"), "method dispatchToEvents");
        check(has(c, "getNode"), "method getNode");
        check(has(c, "needsTwoThreads"), "method needsTwoThreads");
        check(has(c, "getContentHandler"), "method getContentHandler");
        check(has(c, "getLexicalHandler"), "method getLexicalHandler");
        check(has(c, "getEntityResolver"), "method getEntityResolver");
        check(has(c, "getDTDHandler"), "method getDTDHandler");
        check(has(c, "getErrorHandler"), "method getErrorHandler");
        check(has(c, "getDeclHandler"), "method getDeclHandler");
        check(has(c, "appendChild"), "method appendChild");
        check(has(c, "appendTextChild"), "method appendTextChild");
        check(has(c, "getSourceLocatorFor"), "method getSourceLocatorFor");
        check(has(c, "documentRegistration"), "method documentRegistration");
        check(has(c, "documentRelease"), "method documentRelease");
        check(has(c, "migrateTo"), "method migrateTo");
        check(has(c, "release"), "method release");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
