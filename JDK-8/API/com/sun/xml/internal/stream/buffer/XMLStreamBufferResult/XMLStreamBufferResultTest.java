/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.XMLStreamBufferResult conformance: class hierarchy and members.
 * @run main XMLStreamBufferResultTest
 */

public class XMLStreamBufferResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.XMLStreamBufferResult", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.XMLStreamBufferResult"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.transform.sax.SAXResult".equals(c.getSuperclass().getName()), "extends javax.xml.transform.sax.SAXResult");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getHandler"), "has method getHandler");
        check(mn.contains("getLexicalHandler"), "has method getLexicalHandler");
        check(mn.contains("getXMLStreamBuffer"), "has method getXMLStreamBuffer");
        check(mn.contains("setXMLStreamBuffer"), "has method setXMLStreamBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
