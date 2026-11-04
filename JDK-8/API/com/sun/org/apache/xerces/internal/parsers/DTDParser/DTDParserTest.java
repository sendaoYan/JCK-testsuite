/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.DTDParser conformance: abstract class hierarchy and members.
 * @run main DTDParserTest
 */
public class DTDParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.DTDParser", false, DTDParserTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser"), "superclass com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDTDGrammar"), "method getDTDGrammar");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("mixedElement"), "method mixedElement");
        check(mn.contains("childrenStartGroup"), "method childrenStartGroup");
        check(mn.contains("childrenElement"), "method childrenElement");
        check(mn.contains("childrenSeparator"), "method childrenSeparator");
        check(mn.contains("childrenOccurrence"), "method childrenOccurrence");
        check(mn.contains("childrenEndGroup"), "method childrenEndGroup");
        check(mn.contains("endContentModel"), "method endContentModel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
