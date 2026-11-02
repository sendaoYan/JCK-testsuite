/*
 * @test
 * @summary com.sun.xml.internal.bind.marshaller.XMLWriter conformance: class hierarchy and members.
 * @run main XMLWriterTest
 */

public class XMLWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.marshaller.XMLWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.marshaller.XMLWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("org.xml.sax.helpers.XMLFilterImpl".equals(c.getSuperclass().getName()), "extends org.xml.sax.helpers.XMLFilterImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("dataElement"), "has method dataElement");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(mn.contains("init"), "has method init");
        check(mn.contains("processingInstruction"), "has method processingInstruction");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setHeader"), "has method setHeader");
        check(mn.contains("setOutput"), "has method setOutput");
        check(mn.contains("setXmlDecl"), "has method setXmlDecl");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("startPrefixMapping"), "has method startPrefixMapping");
        check(mn.contains("write"), "has method write");
        check(mn.contains("writeAttributes"), "has method writeAttributes");
        check(mn.contains("writeEsc"), "has method writeEsc");
        check(mn.contains("writeXmlDecl"), "has method writeXmlDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
