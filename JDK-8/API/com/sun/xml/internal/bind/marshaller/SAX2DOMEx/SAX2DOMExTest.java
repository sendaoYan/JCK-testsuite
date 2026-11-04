/*
 * @test
 * @summary com.sun.xml.internal.bind.marshaller.SAX2DOMEx conformance: class hierarchy and members.
 * @run main SAX2DOMExTest
 */

public class SAX2DOMExTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.marshaller.SAX2DOMEx", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.marshaller.SAX2DOMEx"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("getCurrentElement"), "has method getCurrentElement");
        check(mn.contains("getDOM"), "has method getDOM");
        check(mn.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(mn.contains("namespace"), "has method namespace");
        check(mn.contains("processingInstruction"), "has method processingInstruction");
        check(mn.contains("setDocumentLocator"), "has method setDocumentLocator");
        check(mn.contains("skippedEntity"), "has method skippedEntity");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("startPrefixMapping"), "has method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
