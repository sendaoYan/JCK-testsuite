/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetWriter conformance: interface hierarchy and members.
 * @run main FastInfosetWriterTest
 */

public class FastInfosetWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 7, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmContentHandler"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmContentHandler");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.ExtendedContentHandler"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.ExtendedContentHandler");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.RestrictedAlphabetContentHandler"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.RestrictedAlphabetContentHandler");
        check(ifs.contains("org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(ifs.contains("org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
