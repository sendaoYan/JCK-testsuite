/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToHTMLStream conformance: class hierarchy and members.
 * @run main ToHTMLStreamTest
 */
public class ToHTMLStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToHTMLStream", false, ToHTMLStreamTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.ToStream"), "superclass com.sun.org.apache.xml.internal.serializer.ToStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("initTagReference"), "method initTagReference");
        check(mn.contains("setSpecialEscapeURLs"), "method setSpecialEscapeURLs");
        check(mn.contains("setOmitMetaTag"), "method setOmitMetaTag");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("getElemDesc"), "method getElemDesc");
        check(mn.contains("startDocumentInternal"), "method startDocumentInternal");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("processAttribute"), "method processAttribute");
        check(mn.contains("writeAttrURI"), "method writeAttrURI");
        check(mn.contains("writeAttrString"), "method writeAttrString");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("cdata"), "method cdata");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("entityReference"), "method entityReference");
        check(mn.contains("processAttributes"), "method processAttributes");
        check(mn.contains("closeStartTag"), "method closeStartTag");
        check(mn.contains("namespaceAfterStartElement"), "method namespaceAfterStartElement");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("addUniqueAttribute"), "method addUniqueAttribute");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
