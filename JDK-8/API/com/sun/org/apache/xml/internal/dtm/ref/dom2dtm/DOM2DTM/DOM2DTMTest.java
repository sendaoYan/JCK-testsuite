/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM conformance: class hierarchy and members.
 * @run main DOM2DTMTest
 */
public class DOM2DTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM", false, DOM2DTMTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addNode"), "method addNode");
        check(mn.contains("getNumberOfNodes"), "method getNumberOfNodes");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("getNode"), "method getNode");
        check(mn.contains("lookupNode"), "method lookupNode");
        check(mn.contains("getNextNodeIdentity"), "method getNextNodeIdentity");
        check(mn.contains("getHandleOfNode"), "method getHandleOfNode");
        check(mn.contains("getAttributeNode"), "method getAttributeNode");
        check(mn.contains("getStringValue"), "method getStringValue");
        check(mn.contains("isWhitespace"), "method isWhitespace");
        check(mn.contains("getNodeData"), "method getNodeData");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeNameX"), "method getNodeNameX");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getDocumentTypeDeclarationSystemIdentifier"), "method getDocumentTypeDeclarationSystemIdentifier");
        check(mn.contains("getDocumentTypeDeclarationPublicIdentifier"), "method getDocumentTypeDeclarationPublicIdentifier");
        check(mn.contains("getElementById"), "method getElementById");
        check(mn.contains("getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(mn.contains("isAttributeSpecified"), "method isAttributeSpecified");
        check(mn.contains("setIncrementalSAXSource"), "method setIncrementalSAXSource");
        check(mn.contains("getContentHandler"), "method getContentHandler");
        check(mn.contains("getLexicalHandler"), "method getLexicalHandler");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("getDeclHandler"), "method getDeclHandler");
        check(mn.contains("needsTwoThreads"), "method needsTwoThreads");
        check(mn.contains("dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(mn.contains("dispatchNodeData"), "method dispatchNodeData");
        check(mn.contains("dispatchToEvents"), "method dispatchToEvents");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getSourceLocatorFor"), "method getSourceLocatorFor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
