/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main SimpleResultTreeImplTest
 */
public class SimpleResultTreeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl", false, SimpleResultTreeImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.EmptySerializer"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.DOM"), "if-com.sun.org.apache.xalan.internal.xsltc.DOM");
        check(ifs.contains("com.sun.org.apache.xml.internal.dtm.DTM"), "if-com.sun.org.apache.xml.internal.dtm.DTM");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 6, "field-count");
        check(fd.contains("RTF_ROOT"), "field-RTF_ROOT");
        check(fd.contains("RTF_TEXT"), "field-RTF_TEXT");
        check(fd.contains("NUMBER_OF_NODES"), "field-NUMBER_OF_NODES");
        check(fd.contains("_textArray"), "field-_textArray");
        check(fd.contains("_dtmManager"), "field-_dtmManager");
        check(fd.contains("_size"), "field-_size");
        check(md.size() == 101, "method-count");
        check(md.contains("appendChild"), "method-appendChild");
        check(md.contains("appendTextChild"), "method-appendTextChild");
        check(md.contains("characters"), "method-characters");
        check(md.contains("copy"), "method-copy");
        check(md.contains("dispatchCharactersEvents"), "method-dispatchCharactersEvents");
        check(md.contains("dispatchToEvents"), "method-dispatchToEvents");
        check(md.contains("documentRegistration"), "method-documentRegistration");
        check(md.contains("documentRelease"), "method-documentRelease");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("getAttributeNode"), "method-getAttributeNode");
        check(md.contains("getAxisIterator"), "method-getAxisIterator");
        check(md.contains("getAxisTraverser"), "method-getAxisTraverser");
        check(md.contains("getChildren"), "method-getChildren");
        check(md.contains("getContentHandler"), "method-getContentHandler");
        check(md.contains("getDTDHandler"), "method-getDTDHandler");
        check(md.contains("getDTMManager"), "method-getDTMManager");
        check(md.contains("getDeclHandler"), "method-getDeclHandler");
        check(md.contains("getDocument"), "method-getDocument");
        check(md.contains("getDocumentAllDeclarationsProcessed"), "method-getDocumentAllDeclarationsProcessed");
        check(md.contains("getDocumentBaseURI"), "method-getDocumentBaseURI");
        check(md.contains("getDocumentEncoding"), "method-getDocumentEncoding");
        check(md.contains("getDocumentRoot"), "method-getDocumentRoot");
        check(md.contains("getDocumentStandalone"), "method-getDocumentStandalone");
        check(md.contains("getDocumentSystemIdentifier"), "method-getDocumentSystemIdentifier");
        check(md.contains("getDocumentTypeDeclarationPublicIdentifier"), "method-getDocumentTypeDeclarationPublicIdentifier");
        check(md.contains("getDocumentTypeDeclarationSystemIdentifier"), "method-getDocumentTypeDeclarationSystemIdentifier");
        check(md.contains("getDocumentURI"), "method-getDocumentURI");
        check(md.contains("getDocumentVersion"), "method-getDocumentVersion");
        check(md.contains("getElementById"), "method-getElementById");
        check(md.contains("getElementsWithIDs"), "method-getElementsWithIDs");
        check(md.contains("getEntityResolver"), "method-getEntityResolver");
        check(md.contains("getErrorHandler"), "method-getErrorHandler");
        check(md.contains("getExpandedTypeID"), "method-getExpandedTypeID");
        check(md.contains("getFirstAttribute"), "method-getFirstAttribute");
        check(md.contains("getFirstChild"), "method-getFirstChild");
        check(md.contains("getFirstNamespaceNode"), "method-getFirstNamespaceNode");
        check(md.contains("getIterator"), "method-getIterator");
        check(md.contains("getLanguage"), "method-getLanguage");
        check(md.contains("getLastChild"), "method-getLastChild");
        check(md.contains("getLevel"), "method-getLevel");
        check(md.contains("getLexicalHandler"), "method-getLexicalHandler");
        check(md.contains("getLocalName"), "method-getLocalName");
        check(md.contains("getLocalNameFromExpandedNameID"), "method-getLocalNameFromExpandedNameID");
        check(md.contains("getNSType"), "method-getNSType");
        check(md.contains("getNamespaceAxisIterator"), "method-getNamespaceAxisIterator");
        check(md.contains("getNamespaceFromExpandedNameID"), "method-getNamespaceFromExpandedNameID");
        check(md.contains("getNamespaceName"), "method-getNamespaceName");
        check(md.contains("getNamespaceType"), "method-getNamespaceType");
        check(md.contains("getNamespaceURI"), "method-getNamespaceURI");
        check(md.contains("getNextAttribute"), "method-getNextAttribute");
        check(md.contains("getNextNamespaceNode"), "method-getNextNamespaceNode");
        check(md.contains("getNextSibling"), "method-getNextSibling");
        check(md.contains("getNode"), "method-getNode");
        check(md.contains("getNodeHandle"), "method-getNodeHandle");
        check(md.contains("getNodeIdent"), "method-getNodeIdent");
        check(md.contains("getNodeName"), "method-getNodeName");
        check(md.contains("getNodeNameX"), "method-getNodeNameX");
        check(md.contains("getNodeType"), "method-getNodeType");
        check(md.contains("getNodeValue"), "method-getNodeValue");
        check(md.contains("getNodeValueIterator"), "method-getNodeValueIterator");
        check(md.contains("getNthDescendant"), "method-getNthDescendant");
        check(md.contains("getOutputDomBuilder"), "method-getOutputDomBuilder");
        check(md.contains("getOwnerDocument"), "method-getOwnerDocument");
        check(md.contains("getParent"), "method-getParent");
        check(md.contains("getPrefix"), "method-getPrefix");
        check(md.contains("getPreviousSibling"), "method-getPreviousSibling");
        check(md.contains("getResultTreeFrag"), "method-getResultTreeFrag");
        check(md.contains("getSize"), "method-getSize");
        check(md.contains("getSourceLocatorFor"), "method-getSourceLocatorFor");
        check(md.contains("getStringValue"), "method-getStringValue");
        check(md.contains("getStringValueChunk"), "method-getStringValueChunk");
        check(md.contains("getStringValueChunkCount"), "method-getStringValueChunkCount");
        check(md.contains("getStringValueX"), "method-getStringValueX");
        check(md.contains("getTypedAxisIterator"), "method-getTypedAxisIterator");
        check(md.contains("getTypedChildren"), "method-getTypedChildren");
        check(md.contains("getUnparsedEntityURI"), "method-getUnparsedEntityURI");
        check(md.contains("hasChildNodes"), "method-hasChildNodes");
        check(md.contains("isAttribute"), "method-isAttribute");
        check(md.contains("isAttributeSpecified"), "method-isAttributeSpecified");
        check(md.contains("isCharacterElementContentWhitespace"), "method-isCharacterElementContentWhitespace");
        check(md.contains("isDocumentAllDeclarationsProcessed"), "method-isDocumentAllDeclarationsProcessed");
        check(md.contains("isElement"), "method-isElement");
        check(md.contains("isNodeAfter"), "method-isNodeAfter");
        check(md.contains("isSupported"), "method-isSupported");
        check(md.contains("lessThan"), "method-lessThan");
        check(md.contains("lookupNamespace"), "method-lookupNamespace");
        check(md.contains("makeNode"), "method-makeNode");
        check(md.contains("makeNodeList"), "method-makeNodeList");
        check(md.contains("migrateTo"), "method-migrateTo");
        check(md.contains("needsTwoThreads"), "method-needsTwoThreads");
        check(md.contains("orderNodes"), "method-orderNodes");
        check(md.contains("release"), "method-release");
        check(md.contains("setDocumentBaseURI"), "method-setDocumentBaseURI");
        check(md.contains("setEscaping"), "method-setEscaping");
        check(md.contains("setFeature"), "method-setFeature");
        check(md.contains("setFilter"), "method-setFilter");
        check(md.contains("setProperty"), "method-setProperty");
        check(md.contains("setupMapping"), "method-setupMapping");
        check(md.contains("shallowCopy"), "method-shallowCopy");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("supportsPreStripping"), "method-supportsPreStripping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
