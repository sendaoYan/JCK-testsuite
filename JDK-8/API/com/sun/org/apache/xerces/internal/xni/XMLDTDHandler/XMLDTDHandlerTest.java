/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLDTDHandler conformance: interface hierarchy and members.
 * @run main XMLDTDHandlerTest
 */
public class XMLDTDHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLDTDHandler", false, XMLDTDHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("setDTDSource"), "method setDTDSource");
        check(mn.contains("getDTDSource"), "method getDTDSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
