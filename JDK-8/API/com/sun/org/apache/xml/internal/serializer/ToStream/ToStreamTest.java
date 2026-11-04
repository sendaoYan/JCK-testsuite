/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToStream conformance: abstract class hierarchy and members.
 * @run main ToStreamTest
 */
public class ToStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToStream", false, ToStreamTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.SerializerBase"), "superclass com.sun.org.apache.xml.internal.serializer.SerializerBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("closeCDATA"), "method closeCDATA");
        check(mn.contains("serialize"), "method serialize");
        check(mn.contains("isUTF16Surrogate"), "method isUTF16Surrogate");
        check(mn.contains("flushWriter"), "method flushWriter");
        check(mn.contains("getOutputStream"), "method getOutputStream");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("outputEntityDecl"), "method outputEntityDecl");
        check(mn.contains("outputLineSep"), "method outputLineSep");
        check(mn.contains("setProp"), "method setProp");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("getOutputFormat"), "method getOutputFormat");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("setLineSepUse"), "method setLineSepUse");
        check(mn.contains("setOutputStream"), "method setOutputStream");
        check(mn.contains("setEscaping"), "method setEscaping");
        check(mn.contains("indent"), "method indent");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("getWriter"), "method getWriter");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("escapingNotNeeded"), "method escapingNotNeeded");
        check(mn.contains("writeUTF16Surrogate"), "method writeUTF16Surrogate");
        check(mn.contains("accumDefaultEntity"), "method accumDefaultEntity");
        check(mn.contains("writeNormalizedChars"), "method writeNormalizedChars");
        check(mn.contains("endNonEscaping"), "method endNonEscaping");
        check(mn.contains("startNonEscaping"), "method startNonEscaping");
        check(mn.contains("cdata"), "method cdata");
        check(mn.contains("charactersRaw"), "method charactersRaw");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("accumDefaultEscape"), "method accumDefaultEscape");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("outputDocTypeDecl"), "method outputDocTypeDecl");
        check(mn.contains("processAttributes"), "method processAttributes");
        check(mn.contains("writeAttrString"), "method writeAttrString");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("closeStartTag"), "method closeStartTag");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("getIndentAmount"), "method getIndentAmount");
        check(mn.contains("setIndentAmount"), "method setIndentAmount");
        check(mn.contains("shouldIndent"), "method shouldIndent");
        check(mn.contains("setCdataSectionElements"), "method setCdataSectionElements");
        check(mn.contains("ensureAttributesNamespaceIsDeclared"), "method ensureAttributesNamespaceIsDeclared");
        check(mn.contains("ensurePrefixIsDeclared"), "method ensurePrefixIsDeclared");
        check(mn.contains("flushPending"), "method flushPending");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("addAttributeAlways"), "method addAttributeAlways");
        check(mn.contains("firePseudoAttributes"), "method firePseudoAttributes");
        check(mn.contains("setTransformer"), "method setTransformer");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("setDTDEntityExpansion"), "method setDTDEntityExpansion");
        check(mn.contains("addCdataSectionElements"), "method addCdataSectionElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
