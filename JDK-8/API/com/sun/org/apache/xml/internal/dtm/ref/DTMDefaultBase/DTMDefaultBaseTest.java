/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase conformance: abstract class hierarchy and members.
 * @run main DTMDefaultBaseTest
 */
public class DTMDefaultBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase", false, DTMDefaultBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.DTM"), "has interface com.sun.org.apache.xml.internal.dtm.DTM");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("ensureSizeOfIndex"), "method ensureSizeOfIndex");
        check(mn.contains("indexNode"), "method indexNode");
        check(mn.contains("findGTE"), "method findGTE");
        check(mn.contains("findElementFromIndex"), "method findElementFromIndex");
        check(mn.contains("getNextNodeIdentity"), "method getNextNodeIdentity");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("getNumberOfNodes"), "method getNumberOfNodes");
        check(mn.contains("_type"), "method _type");
        check(mn.contains("_exptype"), "method _exptype");
        check(mn.contains("_level"), "method _level");
        check(mn.contains("_firstch"), "method _firstch");
        check(mn.contains("_nextsib"), "method _nextsib");
        check(mn.contains("_prevsib"), "method _prevsib");
        check(mn.contains("_parent"), "method _parent");
        check(mn.contains("dumpDTM"), "method dumpDTM");
        check(mn.contains("dumpNode"), "method dumpNode");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("makeNodeHandle"), "method makeNodeHandle");
        check(mn.contains("makeNodeIdentity"), "method makeNodeIdentity");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getTypedFirstChild"), "method getTypedFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getFirstAttribute"), "method getFirstAttribute");
        check(mn.contains("getFirstAttributeIdentity"), "method getFirstAttributeIdentity");
        check(mn.contains("getTypedAttribute"), "method getTypedAttribute");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getTypedNextSibling"), "method getTypedNextSibling");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getNextAttribute"), "method getNextAttribute");
        check(mn.contains("getNextAttributeIdentity"), "method getNextAttributeIdentity");
        check(mn.contains("declareNamespaceInContext"), "method declareNamespaceInContext");
        check(mn.contains("findNamespaceContext"), "method findNamespaceContext");
        check(mn.contains("findInSortedSuballocatedIntVector"), "method findInSortedSuballocatedIntVector");
        check(mn.contains("getFirstNamespaceNode"), "method getFirstNamespaceNode");
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
        check(mn.contains("getNamespaceType"), "method getNamespaceType");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeNameX"), "method getNodeNameX");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getLevel"), "method getLevel");
        check(mn.contains("getNodeIdent"), "method getNodeIdent");
        check(mn.contains("getNodeHandle"), "method getNodeHandle");
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
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("appendTextChild"), "method appendTextChild");
        check(mn.contains("error"), "method error");
        check(mn.contains("getShouldStripWhitespace"), "method getShouldStripWhitespace");
        check(mn.contains("pushShouldStripWhitespace"), "method pushShouldStripWhitespace");
        check(mn.contains("popShouldStripWhitespace"), "method popShouldStripWhitespace");
        check(mn.contains("setShouldStripWhitespace"), "method setShouldStripWhitespace");
        check(mn.contains("documentRegistration"), "method documentRegistration");
        check(mn.contains("documentRelease"), "method documentRelease");
        check(mn.contains("migrateTo"), "method migrateTo");
        check(mn.contains("getManager"), "method getManager");
        check(mn.contains("getDTMIDs"), "method getDTMIDs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
