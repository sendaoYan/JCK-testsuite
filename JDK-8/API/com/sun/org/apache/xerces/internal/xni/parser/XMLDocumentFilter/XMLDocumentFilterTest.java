/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter conformance: interface hierarchy and members.
 * @run main XMLDocumentFilterTest
 */
public class XMLDocumentFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter", false, XMLDocumentFilterTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
