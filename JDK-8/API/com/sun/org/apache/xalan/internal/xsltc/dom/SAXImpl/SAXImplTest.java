/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl conformance: reflection.
 * @run main SAXImplTest
 */

public class SAXImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl", false, SAXImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SAXImpl"), "simple name SAXImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2"), "superclass com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM"), "interface com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.dom.DOMBuilder"), "interface com.sun.org.apache.xalan.internal.xsltc.dom.DOMBuilder");
        check(has(c, "setDocumentURI"), "method setDocumentURI");
        check(has(c, "getDocumentURI"), "method getDocumentURI");
        check(has(c, "setupMapping"), "method setupMapping");
        check(has(c, "lookupNamespace"), "method lookupNamespace");
        check(has(c, "isElement"), "method isElement");
        check(has(c, "isAttribute"), "method isAttribute");
        check(has(c, "getSize"), "method getSize");
        check(has(c, "setFilter"), "method setFilter");
        check(has(c, "lessThan"), "method lessThan");
        check(has(c, "makeNode"), "method makeNode");
        check(has(c, "makeNodeList"), "method makeNodeList");
        check(has(c, "getNodeValueIterator"), "method getNodeValueIterator");
        check(has(c, "orderNodes"), "method orderNodes");
        check(has(c, "getIterator"), "method getIterator");
        check(has(c, "getNSType"), "method getNSType");
        check(has(c, "getNamespaceType"), "method getNamespaceType");
        check(has(c, "getGeneralizedType"), "method getGeneralizedType");
        check(has(c, "getMapping"), "method getMapping");
        check(has(c, "getReverseMapping"), "method getReverseMapping");
        check(has(c, "getNamespaceMapping"), "method getNamespaceMapping");
        check(has(c, "getReverseNamespaceMapping"), "method getReverseNamespaceMapping");
        check(has(c, "migrateTo"), "method migrateTo");
        check(has(c, "getElementById"), "method getElementById");
        check(has(c, "hasDOMSource"), "method hasDOMSource");
        check(has(c, "characters"), "method characters");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "processingInstruction"), "method processingInstruction");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "startPrefixMapping"), "method startPrefixMapping");
        check(has(c, "comment"), "method comment");
        check(has(c, "setEscaping"), "method setEscaping");
        check(has(c, "print"), "method print");
        check(has(c, "getNodeName"), "method getNodeName");
        check(has(c, "getNamespaceName"), "method getNamespaceName");
        check(has(c, "getAttributeNode"), "method getAttributeNode");
        check(has(c, "getAttributeValue"), "method getAttributeValue");
        check(has(c, "getChildren"), "method getChildren");
        check(has(c, "getTypedChildren"), "method getTypedChildren");
        check(has(c, "getAxisIterator"), "method getAxisIterator");
        check(has(c, "getTypedAxisIterator"), "method getTypedAxisIterator");
        check(has(c, "getNamespaceAxisIterator"), "method getNamespaceAxisIterator");
        check(has(c, "getTypedDescendantIterator"), "method getTypedDescendantIterator");
        check(has(c, "getNthDescendant"), "method getNthDescendant");
        check(has(c, "copy"), "method copy");
        check(has(c, "shallowCopy"), "method shallowCopy");
        check(has(c, "getLanguage"), "method getLanguage");
        check(has(c, "getBuilder"), "method getBuilder");
        check(has(c, "getOutputDomBuilder"), "method getOutputDomBuilder");
        check(has(c, "getResultTreeFrag"), "method getResultTreeFrag");
        check(has(c, "getElementsWithIDs"), "method getElementsWithIDs");
        check(has(c, "getUnparsedEntityURI"), "method getUnparsedEntityURI");
        check(has(c, "release"), "method release");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
