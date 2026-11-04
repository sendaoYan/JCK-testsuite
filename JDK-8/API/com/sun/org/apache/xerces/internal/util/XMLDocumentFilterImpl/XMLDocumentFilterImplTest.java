/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLDocumentFilterImpl conformance: class hierarchy and members.
 * @run main XMLDocumentFilterImplTest
 */
public class XMLDocumentFilterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLDocumentFilterImpl", false, XMLDocumentFilterImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("setDocumentSource"), "method setDocumentSource");
        check(mn.contains("getDocumentSource"), "method getDocumentSource");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
