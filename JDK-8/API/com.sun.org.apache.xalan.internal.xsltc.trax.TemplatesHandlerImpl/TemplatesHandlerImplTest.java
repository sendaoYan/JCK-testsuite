/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl conformance: reflection.
 * @run main TemplatesHandlerImplTest
 */

public class TemplatesHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl", false, TemplatesHandlerImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TemplatesHandlerImpl"), "simple name TemplatesHandlerImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.transform.sax.TemplatesHandler"), "interface javax.xml.transform.sax.TemplatesHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader");
        check(has(c, "getSystemId"), "method getSystemId");
        check(has(c, "setSystemId"), "method setSystemId");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "getTemplates"), "method getTemplates");
        check(has(c, "loadSource"), "method loadSource");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startPrefixMapping"), "method startPrefixMapping");
        check(has(c, "endPrefixMapping"), "method endPrefixMapping");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "characters"), "method characters");
        check(has(c, "processingInstruction"), "method processingInstruction");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "skippedEntity"), "method skippedEntity");
        check(has(c, "setDocumentLocator"), "method setDocumentLocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
