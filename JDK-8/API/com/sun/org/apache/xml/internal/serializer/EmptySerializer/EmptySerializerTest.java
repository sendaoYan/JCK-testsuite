/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.EmptySerializer conformance: class hierarchy and members.
 * @run main EmptySerializerTest
 */
public class EmptySerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.EmptySerializer", false, EmptySerializerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.SerializationHandler"), "has interface com.sun.org.apache.xml.internal.serializer.SerializationHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("couldThrowIOException"), "method couldThrowIOException");
        check(mn.contains("couldThrowSAXException"), "method couldThrowSAXException");
        check(mn.contains("aMethodIsCalled"), "method aMethodIsCalled");
        check(mn.contains("asContentHandler"), "method asContentHandler");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("close"), "method close");
        check(mn.contains("getOutputFormat"), "method getOutputFormat");
        check(mn.contains("getOutputStream"), "method getOutputStream");
        check(mn.contains("getWriter"), "method getWriter");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("serialize"), "method serialize");
        check(mn.contains("setCdataSectionElements"), "method setCdataSectionElements");
        check(mn.contains("setEscaping"), "method setEscaping");
        check(mn.contains("setIndent"), "method setIndent");
        check(mn.contains("setIndentAmount"), "method setIndentAmount");
        check(mn.contains("setIsStandalone"), "method setIsStandalone");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("setOutputStream"), "method setOutputStream");
        check(mn.contains("setVersion"), "method setVersion");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("setTransformer"), "method setTransformer");
        check(mn.contains("getTransformer"), "method getTransformer");
        check(mn.contains("flushPending"), "method flushPending");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("addAttributes"), "method addAttributes");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("namespaceAfterStartElement"), "method namespaceAfterStartElement");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("entityReference"), "method entityReference");
        check(mn.contains("getNamespaceMappings"), "method getNamespaceMappings");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getNamespaceURIFromPrefix"), "method getNamespaceURIFromPrefix");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("getDoctypePublic"), "method getDoctypePublic");
        check(mn.contains("getDoctypeSystem"), "method getDoctypeSystem");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getIndent"), "method getIndent");
        check(mn.contains("getIndentAmount"), "method getIndentAmount");
        check(mn.contains("getMediaType"), "method getMediaType");
        check(mn.contains("getOmitXMLDeclaration"), "method getOmitXMLDeclaration");
        check(mn.contains("getStandalone"), "method getStandalone");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("setDoctype"), "method setDoctype");
        check(mn.contains("setDoctypePublic"), "method setDoctypePublic");
        check(mn.contains("setDoctypeSystem"), "method setDoctypeSystem");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("setMediaType"), "method setMediaType");
        check(mn.contains("setOmitXMLDeclaration"), "method setOmitXMLDeclaration");
        check(mn.contains("setStandalone"), "method setStandalone");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("asDOMSerializer"), "method asDOMSerializer");
        check(mn.contains("setNamespaceMappings"), "method setNamespaceMappings");
        check(mn.contains("setSourceLocator"), "method setSourceLocator");
        check(mn.contains("addUniqueAttribute"), "method addUniqueAttribute");
        check(mn.contains("addXSLAttribute"), "method addXSLAttribute");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("setDTDEntityExpansion"), "method setDTDEntityExpansion");
        check(mn.contains("getOutputProperty"), "method getOutputProperty");
        check(mn.contains("getOutputPropertyDefault"), "method getOutputPropertyDefault");
        check(mn.contains("setOutputProperty"), "method setOutputProperty");
        check(mn.contains("setOutputPropertyDefault"), "method setOutputPropertyDefault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
