/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.DOMBuilder conformance: class hierarchy and members.
 * @run main DOMBuilderTest
 */
public class DOMBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.DOMBuilder", false, DOMBuilderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getRootDocument"), "method getRootDocument");
        check(mn.contains("getRootNode"), "method getRootNode");
        check(mn.contains("getCurrentNode"), "method getCurrentNode");
        check(mn.contains("setNextSibling"), "method setNextSibling");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getWriter"), "method getWriter");
        check(mn.contains("append"), "method append");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("setIDAttribute"), "method setIDAttribute");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("charactersRaw"), "method charactersRaw");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("entityReference"), "method entityReference");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("cdata"), "method cdata");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
