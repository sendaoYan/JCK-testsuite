/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main SAXImplTest
 */
public class SAXImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl", false, SAXImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM"), "if-com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.dom.DOMBuilder"), "if-com.sun.org.apache.xalan.internal.xsltc.dom.DOMBuilder");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 56, "method-count");
        check(md.contains("characters"), "method-characters");
        check(md.contains("comment"), "method-comment");
        check(md.contains("copy"), "method-copy");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("getAttributeNode"), "method-getAttributeNode");
        check(md.contains("getAttributeValue"), "method-getAttributeValue");
        check(md.contains("getAxisIterator"), "method-getAxisIterator");
        check(md.contains("getBuilder"), "method-getBuilder");
        check(md.contains("getChildren"), "method-getChildren");
        check(md.contains("getDocumentURI"), "method-getDocumentURI");
        check(md.contains("getElementById"), "method-getElementById");
        check(md.contains("getElementsWithIDs"), "method-getElementsWithIDs");
        check(md.contains("getGeneralizedType"), "method-getGeneralizedType");
        check(md.contains("getIterator"), "method-getIterator");
        check(md.contains("getLanguage"), "method-getLanguage");
        check(md.contains("getMapping"), "method-getMapping");
        check(md.contains("getNSType"), "method-getNSType");
        check(md.contains("getNamespaceAxisIterator"), "method-getNamespaceAxisIterator");
        check(md.contains("getNamespaceMapping"), "method-getNamespaceMapping");
        check(md.contains("getNamespaceName"), "method-getNamespaceName");
        check(md.contains("getNamespaceType"), "method-getNamespaceType");
        check(md.contains("getNodeName"), "method-getNodeName");
        check(md.contains("getNodeValueIterator"), "method-getNodeValueIterator");
        check(md.contains("getNthDescendant"), "method-getNthDescendant");
        check(md.contains("getOutputDomBuilder"), "method-getOutputDomBuilder");
        check(md.contains("getResultTreeFrag"), "method-getResultTreeFrag");
        check(md.contains("getReverseMapping"), "method-getReverseMapping");
        check(md.contains("getReverseNamespaceMapping"), "method-getReverseNamespaceMapping");
        check(md.contains("getShouldStripWhitespace"), "method-getShouldStripWhitespace");
        check(md.contains("getSize"), "method-getSize");
        check(md.contains("getTypedAxisIterator"), "method-getTypedAxisIterator");
        check(md.contains("getTypedChildren"), "method-getTypedChildren");
        check(md.contains("getTypedDescendantIterator"), "method-getTypedDescendantIterator");
        check(md.contains("getUnparsedEntityURI"), "method-getUnparsedEntityURI");
        check(md.contains("hasDOMSource"), "method-hasDOMSource");
        check(md.contains("ignorableWhitespace"), "method-ignorableWhitespace");
        check(md.contains("isAttribute"), "method-isAttribute");
        check(md.contains("isElement"), "method-isElement");
        check(md.contains("lessThan"), "method-lessThan");
        check(md.contains("lookupNamespace"), "method-lookupNamespace");
        check(md.contains("makeNode"), "method-makeNode");
        check(md.contains("makeNodeList"), "method-makeNodeList");
        check(md.contains("migrateTo"), "method-migrateTo");
        check(md.contains("orderNodes"), "method-orderNodes");
        check(md.contains("print"), "method-print");
        check(md.contains("processingInstruction"), "method-processingInstruction");
        check(md.contains("release"), "method-release");
        check(md.contains("setDocumentURI"), "method-setDocumentURI");
        check(md.contains("setEscaping"), "method-setEscaping");
        check(md.contains("setFilter"), "method-setFilter");
        check(md.contains("setupMapping"), "method-setupMapping");
        check(md.contains("shallowCopy"), "method-shallowCopy");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("startPrefixMapping"), "method-startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
