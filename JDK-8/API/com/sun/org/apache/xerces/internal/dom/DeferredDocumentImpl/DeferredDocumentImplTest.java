/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl conformance: class hierarchy and members.
 * @run main DeferredDocumentImplTest
 */
public class DeferredDocumentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl", false, DeferredDocumentImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.DocumentImpl"), "superclass com.sun.org.apache.xerces.internal.dom.DocumentImpl");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.dom.DeferredNode"), "has interface com.sun.org.apache.xerces.internal.dom.DeferredNode");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("getNamespacesEnabled"), "method getNamespacesEnabled");
        check(mn.contains("setNamespacesEnabled"), "method setNamespacesEnabled");
        check(mn.contains("createDeferredDocument"), "method createDeferredDocument");
        check(mn.contains("createDeferredDocumentType"), "method createDeferredDocumentType");
        check(mn.contains("setInternalSubset"), "method setInternalSubset");
        check(mn.contains("createDeferredNotation"), "method createDeferredNotation");
        check(mn.contains("createDeferredEntity"), "method createDeferredEntity");
        check(mn.contains("getDeferredEntityBaseURI"), "method getDeferredEntityBaseURI");
        check(mn.contains("setEntityInfo"), "method setEntityInfo");
        check(mn.contains("setTypeInfo"), "method setTypeInfo");
        check(mn.contains("setInputEncoding"), "method setInputEncoding");
        check(mn.contains("createDeferredEntityReference"), "method createDeferredEntityReference");
        check(mn.contains("createDeferredElement"), "method createDeferredElement");
        check(mn.contains("setDeferredAttribute"), "method setDeferredAttribute");
        check(mn.contains("createDeferredAttribute"), "method createDeferredAttribute");
        check(mn.contains("createDeferredElementDefinition"), "method createDeferredElementDefinition");
        check(mn.contains("createDeferredTextNode"), "method createDeferredTextNode");
        check(mn.contains("createDeferredCDATASection"), "method createDeferredCDATASection");
        check(mn.contains("createDeferredProcessingInstruction"), "method createDeferredProcessingInstruction");
        check(mn.contains("createDeferredComment"), "method createDeferredComment");
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("appendChild"), "method appendChild");
        check(mn.contains("setAttributeNode"), "method setAttributeNode");
        check(mn.contains("setIdAttributeNode"), "method setIdAttributeNode");
        check(mn.contains("setIdAttribute"), "method setIdAttribute");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("setAsLastChild"), "method setAsLastChild");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("getPrevSibling"), "method getPrevSibling");
        check(mn.contains("getRealPrevSibling"), "method getRealPrevSibling");
        check(mn.contains("lookupElementDefinition"), "method lookupElementDefinition");
        check(mn.contains("getNodeObject"), "method getNodeObject");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getNodeValueString"), "method getNodeValueString");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("getTypeInfo"), "method getTypeInfo");
        check(mn.contains("getNodeExtra"), "method getNodeExtra");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getNodeURI"), "method getNodeURI");
        check(mn.contains("putIdentifier"), "method putIdentifier");
        check(mn.contains("print"), "method print");
        check(mn.contains("getNodeIndex"), "method getNodeIndex");
        check(mn.contains("synchronizeData"), "method synchronizeData");
        check(mn.contains("synchronizeChildren"), "method synchronizeChildren");
        check(mn.contains("ensureCapacity"), "method ensureCapacity");
        check(mn.contains("createNode"), "method createNode");
        check(mn.contains("binarySearch"), "method binarySearch");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
