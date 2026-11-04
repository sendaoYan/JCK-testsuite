/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader conformance: interface hierarchy and members.
 * @run main FastInfosetReaderTest
 */

public class FastInfosetReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetParser"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetParser");
        check(ifs.contains("org.xml.sax.XMLReader"), "has interface org.xml.sax.XMLReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDeclHandler"), "has method getDeclHandler");
        check(mn.contains("getEncodingAlgorithmContentHandler"), "has method getEncodingAlgorithmContentHandler");
        check(mn.contains("getLexicalHandler"), "has method getLexicalHandler");
        check(mn.contains("getPrimitiveTypeContentHandler"), "has method getPrimitiveTypeContentHandler");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("setDeclHandler"), "has method setDeclHandler");
        check(mn.contains("setEncodingAlgorithmContentHandler"), "has method setEncodingAlgorithmContentHandler");
        check(mn.contains("setLexicalHandler"), "has method setLexicalHandler");
        check(mn.contains("setPrimitiveTypeContentHandler"), "has method setPrimitiveTypeContentHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
