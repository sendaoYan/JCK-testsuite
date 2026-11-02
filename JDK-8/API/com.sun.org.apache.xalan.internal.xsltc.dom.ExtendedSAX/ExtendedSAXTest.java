/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.ExtendedSAX conformance: reflection.
 * @run main ExtendedSAXTest
 */

public class ExtendedSAXTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.ExtendedSAX", false, ExtendedSAXTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExtendedSAX"), "simple name ExtendedSAX");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
