/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerHandlerImpl conformance: reflection.
 * @run main TransformerHandlerImplTest
 */

public class TransformerHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerHandlerImpl", false, TransformerHandlerImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TransformerHandlerImpl"), "simple name TransformerHandlerImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.transform.sax.TransformerHandler"), "interface javax.xml.transform.sax.TransformerHandler");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        check(has(c, "getSystemId"), "method getSystemId");
        check(has(c, "setSystemId"), "method setSystemId");
        check(has(c, "getTransformer"), "method getTransformer");
        check(has(c, "setResult"), "method setResult");
        check(has(c, "characters"), "method characters");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "processingInstruction"), "method processingInstruction");
        check(has(c, "startCDATA"), "method startCDATA");
        check(has(c, "endCDATA"), "method endCDATA");
        check(has(c, "comment"), "method comment");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "setDocumentLocator"), "method setDocumentLocator");
        check(has(c, "skippedEntity"), "method skippedEntity");
        check(has(c, "startPrefixMapping"), "method startPrefixMapping");
        check(has(c, "endPrefixMapping"), "method endPrefixMapping");
        check(has(c, "startDTD"), "method startDTD");
        check(has(c, "endDTD"), "method endDTD");
        check(has(c, "startEntity"), "method startEntity");
        check(has(c, "endEntity"), "method endEntity");
        check(has(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
        check(has(c, "notationDecl"), "method notationDecl");
        check(has(c, "attributeDecl"), "method attributeDecl");
        check(has(c, "elementDecl"), "method elementDecl");
        check(has(c, "externalEntityDecl"), "method externalEntityDecl");
        check(has(c, "internalEntityDecl"), "method internalEntityDecl");
        check(has(c, "reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
