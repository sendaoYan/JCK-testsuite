/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler conformance: interface hierarchy and members.
 * @run main XMLDocumentHandlerTest
 */
public class XMLDocumentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler", false, XMLDocumentHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
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
