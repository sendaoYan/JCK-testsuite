/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.sax.SAXBufferCreator conformance: class hierarchy and members.
 * @run main SAXBufferCreatorTest
 */

public class SAXBufferCreatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.sax.SAXBufferCreator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.sax.SAXBufferCreator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.AbstractCreator".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractCreator");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 5, "direct interface count");
        check(ifs.contains("org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(ifs.contains("org.xml.sax.DTDHandler"), "has interface org.xml.sax.DTDHandler");
        check(ifs.contains("org.xml.sax.EntityResolver"), "has interface org.xml.sax.EntityResolver");
        check(ifs.contains("org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        check(ifs.contains("org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cacheNamespaceAttribute"), "has method cacheNamespaceAttribute");
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("create"), "has method create");
        check(mn.contains("endCDATA"), "has method endCDATA");
        check(mn.contains("endDTD"), "has method endDTD");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endEntity"), "has method endEntity");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("error"), "has method error");
        check(mn.contains("fatalError"), "has method fatalError");
        check(mn.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(mn.contains("notationDecl"), "has method notationDecl");
        check(mn.contains("processingInstruction"), "has method processingInstruction");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("resolveEntity"), "has method resolveEntity");
        check(mn.contains("setDocumentLocator"), "has method setDocumentLocator");
        check(mn.contains("skippedEntity"), "has method skippedEntity");
        check(mn.contains("startCDATA"), "has method startCDATA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
