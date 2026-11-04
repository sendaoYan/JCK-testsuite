/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer conformance: abstract class hierarchy and members.
 * @run main BaseMarkupSerializerTest
 */
public class BaseMarkupSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer", false, BaseMarkupSerializerTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "has interface org.xml.sax.DocumentHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "has interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "has interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serialize.DOMSerializer"), "has interface com.sun.org.apache.xml.internal.serialize.DOMSerializer");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serialize.Serializer"), "has interface com.sun.org.apache.xml.internal.serialize.Serializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asDocumentHandler"), "method asDocumentHandler");
        check(mn.contains("asContentHandler"), "method asContentHandler");
        check(mn.contains("asDOMSerializer"), "method asDOMSerializer");
        check(mn.contains("setOutputByteStream"), "method setOutputByteStream");
        check(mn.contains("setOutputCharStream"), "method setOutputCharStream");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("cleanup"), "method cleanup");
        check(mn.contains("prepare"), "method prepare");
        check(mn.contains("serialize"), "method serialize");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("processingInstructionIO"), "method processingInstructionIO");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("startNonEscaping"), "method startNonEscaping");
        check(mn.contains("endNonEscaping"), "method endNonEscaping");
        check(mn.contains("startPreserving"), "method startPreserving");
        check(mn.contains("endPreserving"), "method endPreserving");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("serializeNode"), "method serializeNode");
        check(mn.contains("serializeDocument"), "method serializeDocument");
        check(mn.contains("serializeDTD"), "method serializeDTD");
        check(mn.contains("content"), "method content");
        check(mn.contains("getEntityRef"), "method getEntityRef");
        check(mn.contains("serializeElement"), "method serializeElement");
        check(mn.contains("serializePreRoot"), "method serializePreRoot");
        check(mn.contains("printCDATAText"), "method printCDATAText");
        check(mn.contains("surrogates"), "method surrogates");
        check(mn.contains("printText"), "method printText");
        check(mn.contains("printDoctypeURL"), "method printDoctypeURL");
        check(mn.contains("printEscaped"), "method printEscaped");
        check(mn.contains("printHex"), "method printHex");
        check(mn.contains("getElementState"), "method getElementState");
        check(mn.contains("enterElementState"), "method enterElementState");
        check(mn.contains("leaveElementState"), "method leaveElementState");
        check(mn.contains("isDocumentState"), "method isDocumentState");
        check(mn.contains("clearDocumentState"), "method clearDocumentState");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("modifyDOMError"), "method modifyDOMError");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("checkUnboundNamespacePrefixedNode"), "method checkUnboundNamespacePrefixedNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
