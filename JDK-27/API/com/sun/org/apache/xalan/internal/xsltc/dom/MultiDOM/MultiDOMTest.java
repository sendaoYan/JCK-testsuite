/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.MultiDOM conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main MultiDOMTest
 */
public class MultiDOMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.MultiDOM", false, MultiDOMTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.MultiDOM"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.DOM"), "if-com.sun.org.apache.xalan.internal.xsltc.DOM");
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
        check(md.size() == 50, "method-count");
        check(md.contains("addDOMAdapter"), "method-addDOMAdapter");
        check(md.contains("characters"), "method-characters");
        check(md.contains("copy"), "method-copy");
        check(md.contains("getAttributeNode"), "method-getAttributeNode");
        check(md.contains("getAxisIterator"), "method-getAxisIterator");
        check(md.contains("getChildren"), "method-getChildren");
        check(md.contains("getDOMAdapter"), "method-getDOMAdapter");
        check(md.contains("getDTM"), "method-getDTM");
        check(md.contains("getDTMId"), "method-getDTMId");
        check(md.contains("getDTMManager"), "method-getDTMManager");
        check(md.contains("getDocument"), "method-getDocument");
        check(md.contains("getDocumentMask"), "method-getDocumentMask");
        check(md.contains("getDocumentURI"), "method-getDocumentURI");
        check(md.contains("getElementsWithIDs"), "method-getElementsWithIDs");
        check(md.contains("getExpandedTypeID"), "method-getExpandedTypeID");
        check(md.contains("getIterator"), "method-getIterator");
        check(md.contains("getLanguage"), "method-getLanguage");
        check(md.contains("getMain"), "method-getMain");
        check(md.contains("getNSType"), "method-getNSType");
        check(md.contains("getNamespaceAxisIterator"), "method-getNamespaceAxisIterator");
        check(md.contains("getNamespaceName"), "method-getNamespaceName");
        check(md.contains("getNamespaceType"), "method-getNamespaceType");
        check(md.contains("getNodeHandle"), "method-getNodeHandle");
        check(md.contains("getNodeIdent"), "method-getNodeIdent");
        check(md.contains("getNodeName"), "method-getNodeName");
        check(md.contains("getNodeNameX"), "method-getNodeNameX");
        check(md.contains("getNodeValueIterator"), "method-getNodeValueIterator");
        check(md.contains("getNthDescendant"), "method-getNthDescendant");
        check(md.contains("getOutputDomBuilder"), "method-getOutputDomBuilder");
        check(md.contains("getParent"), "method-getParent");
        check(md.contains("getResultTreeFrag"), "method-getResultTreeFrag");
        check(md.contains("getSize"), "method-getSize");
        check(md.contains("getStringValue"), "method-getStringValue");
        check(md.contains("getStringValueX"), "method-getStringValueX");
        check(md.contains("getTypedAxisIterator"), "method-getTypedAxisIterator");
        check(md.contains("getTypedChildren"), "method-getTypedChildren");
        check(md.contains("getUnparsedEntityURI"), "method-getUnparsedEntityURI");
        check(md.contains("isAttribute"), "method-isAttribute");
        check(md.contains("isElement"), "method-isElement");
        check(md.contains("lessThan"), "method-lessThan");
        check(md.contains("lookupNamespace"), "method-lookupNamespace");
        check(md.contains("makeNode"), "method-makeNode");
        check(md.contains("makeNodeList"), "method-makeNodeList");
        check(md.contains("nextMask"), "method-nextMask");
        check(md.contains("orderNodes"), "method-orderNodes");
        check(md.contains("release"), "method-release");
        check(md.contains("removeDOMAdapter"), "method-removeDOMAdapter");
        check(md.contains("setFilter"), "method-setFilter");
        check(md.contains("setupMapping"), "method-setupMapping");
        check(md.contains("shallowCopy"), "method-shallowCopy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
