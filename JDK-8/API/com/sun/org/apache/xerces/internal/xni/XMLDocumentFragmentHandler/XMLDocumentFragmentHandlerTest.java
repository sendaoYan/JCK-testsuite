/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLDocumentFragmentHandler conformance: interface hierarchy and members.
 * @run main XMLDocumentFragmentHandlerTest
 */
public class XMLDocumentFragmentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLDocumentFragmentHandler", false, XMLDocumentFragmentHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocumentFragment"), "method startDocumentFragment");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocumentFragment"), "method endDocumentFragment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
