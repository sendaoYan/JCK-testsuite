/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter conformance: interface hierarchy and members.
 * @run main XMLDTDFilterTest
 */
public class XMLDTDFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter", false, XMLDTDFilterTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
