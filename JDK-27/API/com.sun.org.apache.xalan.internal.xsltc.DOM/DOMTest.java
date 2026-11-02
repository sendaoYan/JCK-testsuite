/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.DOM conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc:+open
 * @run main DOMTest
 */
public class DOMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.DOM", false, DOMTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.DOM"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
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
        check(fd.size() == 8, "field-count");
        check(fd.contains("FIRST_TYPE"), "field-FIRST_TYPE");
        check(fd.contains("NO_TYPE"), "field-NO_TYPE");
        check(fd.contains("NULL"), "field-NULL");
        check(fd.contains("RETURN_CURRENT"), "field-RETURN_CURRENT");
        check(fd.contains("RETURN_PARENT"), "field-RETURN_PARENT");
        check(fd.contains("SIMPLE_RTF"), "field-SIMPLE_RTF");
        check(fd.contains("ADAPTIVE_RTF"), "field-ADAPTIVE_RTF");
        check(fd.contains("TREE_RTF"), "field-TREE_RTF");
        check(md.size() == 41, "method-count");
        check(md.contains("characters"), "method-characters");
        check(md.contains("copy"), "method-copy");
        check(md.contains("getAttributeNode"), "method-getAttributeNode");
        check(md.contains("getAxisIterator"), "method-getAxisIterator");
        check(md.contains("getChildren"), "method-getChildren");
        check(md.contains("getDocument"), "method-getDocument");
        check(md.contains("getDocumentURI"), "method-getDocumentURI");
        check(md.contains("getElementsWithIDs"), "method-getElementsWithIDs");
        check(md.contains("getExpandedTypeID"), "method-getExpandedTypeID");
        check(md.contains("getIterator"), "method-getIterator");
        check(md.contains("getLanguage"), "method-getLanguage");
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
        check(md.contains("orderNodes"), "method-orderNodes");
        check(md.contains("release"), "method-release");
        check(md.contains("setFilter"), "method-setFilter");
        check(md.contains("setupMapping"), "method-setupMapping");
        check(md.contains("shallowCopy"), "method-shallowCopy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
