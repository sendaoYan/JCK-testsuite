/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMNormalizer conformance: class hierarchy and members.
 * @run main DOMNormalizerTest
 */
public class DOMNormalizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMNormalizer", false, DOMNormalizerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("normalizeDocument"), "method normalizeDocument");
        check(mn.contains("normalizeNode"), "method normalizeNode");
        check(mn.contains("expandEntityRef"), "method expandEntityRef");
        check(mn.contains("namespaceFixUp"), "method namespaceFixUp");
        check(mn.contains("addNamespaceDecl"), "method addNamespaceDecl");
        check(mn.contains("isCDataWF"), "method isCDataWF");
        check(mn.contains("isXMLCharWF"), "method isXMLCharWF");
        check(mn.contains("isCommentWF"), "method isCommentWF");
        check(mn.contains("isAttrValueWF"), "method isAttrValueWF");
        check(mn.contains("reportDOMError"), "method reportDOMError");
        check(mn.contains("updateQName"), "method updateQName");
        check(mn.contains("normalizeAttributeValue"), "method normalizeAttributeValue");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("setDocumentSource"), "method setDocumentSource");
        check(mn.contains("getDocumentSource"), "method getDocumentSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
