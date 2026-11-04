/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToHTMLSAXHandler conformance: class hierarchy and members.
 * @run main ToHTMLSAXHandlerTest
 */
public class ToHTMLSAXHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToHTMLSAXHandler", false, ToHTMLSAXHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.ToSAXHandler"), "superclass com.sun.org.apache.xml.internal.serializer.ToSAXHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getOutputFormat"), "method getOutputFormat");
        check(mn.contains("getOutputStream"), "method getOutputStream");
        check(mn.contains("getWriter"), "method getWriter");
        check(mn.contains("indent"), "method indent");
        check(mn.contains("serialize"), "method serialize");
        check(mn.contains("setEscaping"), "method setEscaping");
        check(mn.contains("setIndent"), "method setIndent");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("setOutputStream"), "method setOutputStream");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("closeStartTag"), "method closeStartTag");
        check(mn.contains("close"), "method close");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("flushPending"), "method flushPending");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("namespaceAfterStartElement"), "method namespaceAfterStartElement");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
