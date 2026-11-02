/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.tools.ResolvingXMLFilter conformance: class hierarchy and members.
 * @run main ResolvingXMLFilterTest
 */
public class ResolvingXMLFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.tools.ResolvingXMLFilter", false, ResolvingXMLFilterTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.XMLFilterImpl"), "superclass org.xml.sax.helpers.XMLFilterImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCatalog"), "method getCatalog");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
