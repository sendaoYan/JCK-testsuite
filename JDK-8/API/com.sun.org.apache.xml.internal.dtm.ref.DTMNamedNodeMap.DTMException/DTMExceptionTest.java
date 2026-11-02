/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMNamedNodeMap.DTMException conformance: class hierarchy and members.
 * @run main DTMExceptionTest
 */
public class DTMExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMNamedNodeMap$DTMException", false, DTMExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.w3c.dom.DOMException"), "superclass org.w3c.dom.DOMException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
