/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter conformance: reflection.
 * @run main DOMAdapterTest
 */

public class DOMAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter", false, DOMAdapterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DOMAdapter"), "simple name DOMAdapter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.DOM"), "interface com.sun.org.apache.xalan.internal.xsltc.DOM");
        check(has(c, "setupMapping"), "method setupMapping");
        check(has(c, "getNamesArray"), "method getNamesArray");
        check(has(c, "getUrisArray"), "method getUrisArray");
        check(has(c, "getTypesArray"), "method getTypesArray");
        check(has(c, "getNamespaceArray"), "method getNamespaceArray");
        check(has(c, "getDOMImpl"), "method getDOMImpl");
        check(has(c, "getIterator"), "method getIterator");
        check(has(c, "getStringValue"), "method getStringValue");
        check(has(c, "getChildren"), "method getChildren");
        check(has(c, "setFilter"), "method setFilter");
        check(has(c, "getTypedChildren"), "method getTypedChildren");
        check(has(c, "getNamespaceAxisIterator"), "method getNamespaceAxisIterator");
        check(has(c, "getAxisIterator"), "method getAxisIterator");
        check(has(c, "getTypedAxisIterator"), "method getTypedAxisIterator");
        check(has(c, "getMultiDOMMask"), "method getMultiDOMMask");
        check(has(c, "setMultiDOMMask"), "method setMultiDOMMask");
        check(has(c, "getNthDescendant"), "method getNthDescendant");
        check(has(c, "getNodeValueIterator"), "method getNodeValueIterator");
        check(has(c, "orderNodes"), "method orderNodes");
        check(has(c, "getExpandedTypeID"), "method getExpandedTypeID");
        check(has(c, "getNamespaceType"), "method getNamespaceType");
        check(has(c, "getNSType"), "method getNSType");
        check(has(c, "getParent"), "method getParent");
        check(has(c, "getAttributeNode"), "method getAttributeNode");
        check(has(c, "getNodeName"), "method getNodeName");
        check(has(c, "getNodeNameX"), "method getNodeNameX");
        check(has(c, "getNamespaceName"), "method getNamespaceName");
        check(has(c, "getStringValueX"), "method getStringValueX");
        check(has(c, "copy"), "method copy");
        check(has(c, "shallowCopy"), "method shallowCopy");
        check(has(c, "lessThan"), "method lessThan");
        check(has(c, "characters"), "method characters");
        check(has(c, "makeNode"), "method makeNode");
        check(has(c, "makeNodeList"), "method makeNodeList");
        check(has(c, "getLanguage"), "method getLanguage");
        check(has(c, "getSize"), "method getSize");
        check(has(c, "setDocumentURI"), "method setDocumentURI");
        check(has(c, "getDocumentURI"), "method getDocumentURI");
        check(has(c, "getDocument"), "method getDocument");
        check(has(c, "isElement"), "method isElement");
        check(has(c, "isAttribute"), "method isAttribute");
        check(has(c, "getNodeIdent"), "method getNodeIdent");
        check(has(c, "getNodeHandle"), "method getNodeHandle");
        check(has(c, "getResultTreeFrag"), "method getResultTreeFrag");
        check(has(c, "getOutputDomBuilder"), "method getOutputDomBuilder");
        check(has(c, "lookupNamespace"), "method lookupNamespace");
        check(has(c, "getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(has(c, "getElementsWithIDs"), "method getElementsWithIDs");
        check(has(c, "release"), "method release");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
