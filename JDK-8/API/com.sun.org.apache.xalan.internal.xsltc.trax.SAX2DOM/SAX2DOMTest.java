/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM conformance: reflection.
 * @run main SAX2DOMTest
 */

public class SAX2DOMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM", false, SAX2DOMTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SAX2DOM"), "simple name SAX2DOM");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.runtime.Constants"), "interface com.sun.org.apache.xalan.internal.xsltc.runtime.Constants");
        check(has(c, "getDOM"), "method getDOM");
        check(has(c, "characters"), "method characters");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "startPrefixMapping"), "method startPrefixMapping");
        check(has(c, "endPrefixMapping"), "method endPrefixMapping");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "processingInstruction"), "method processingInstruction");
        check(has(c, "setDocumentLocator"), "method setDocumentLocator");
        check(has(c, "skippedEntity"), "method skippedEntity");
        check(has(c, "comment"), "method comment");
        check(has(c, "startCDATA"), "method startCDATA");
        check(has(c, "endCDATA"), "method endCDATA");
        check(has(c, "startEntity"), "method startEntity");
        check(has(c, "endDTD"), "method endDTD");
        check(has(c, "endEntity"), "method endEntity");
        check(has(c, "startDTD"), "method startDTD");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
