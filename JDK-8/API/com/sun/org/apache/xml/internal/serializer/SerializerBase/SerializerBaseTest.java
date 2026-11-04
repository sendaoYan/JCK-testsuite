/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.SerializerBase conformance: abstract class hierarchy and members.
 * @run main SerializerBaseTest
 */
public class SerializerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.SerializerBase", false, SerializerBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.SerializationHandler"), "has interface com.sun.org.apache.xml.internal.serializer.SerializationHandler");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.SerializerConstants"), "has interface com.sun.org.apache.xml.internal.serializer.SerializerConstants");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fireEndElem"), "method fireEndElem");
        check(mn.contains("fireCharEvent"), "method fireCharEvent");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("patchName"), "method patchName");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("addAttributeAlways"), "method addAttributeAlways");
        check(mn.contains("addXSLAttribute"), "method addXSLAttribute");
        check(mn.contains("addAttributes"), "method addAttributes");
        check(mn.contains("asContentHandler"), "method asContentHandler");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("close"), "method close");
        check(mn.contains("initCDATA"), "method initCDATA");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("setOmitXMLDeclaration"), "method setOmitXMLDeclaration");
        check(mn.contains("getOmitXMLDeclaration"), "method getOmitXMLDeclaration");
        check(mn.contains("getDoctypePublic"), "method getDoctypePublic");
        check(mn.contains("setDoctypePublic"), "method setDoctypePublic");
        check(mn.contains("getDoctypeSystem"), "method getDoctypeSystem");
        check(mn.contains("setDoctypeSystem"), "method setDoctypeSystem");
        check(mn.contains("setDoctype"), "method setDoctype");
        check(mn.contains("setStandalone"), "method setStandalone");
        check(mn.contains("setStandaloneInternal"), "method setStandaloneInternal");
        check(mn.contains("getStandalone"), "method getStandalone");
        check(mn.contains("getIndent"), "method getIndent");
        check(mn.contains("getMediaType"), "method getMediaType");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("setVersion"), "method setVersion");
        check(mn.contains("setMediaType"), "method setMediaType");
        check(mn.contains("getIndentAmount"), "method getIndentAmount");
        check(mn.contains("setIndentAmount"), "method setIndentAmount");
        check(mn.contains("setIndent"), "method setIndent");
        check(mn.contains("setIsStandalone"), "method setIsStandalone");
        check(mn.contains("namespaceAfterStartElement"), "method namespaceAfterStartElement");
        check(mn.contains("asDOMSerializer"), "method asDOMSerializer");
        check(mn.contains("getPrefixPart"), "method getPrefixPart");
        check(mn.contains("getNamespaceMappings"), "method getNamespaceMappings");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getNamespaceURIFromPrefix"), "method getNamespaceURIFromPrefix");
        check(mn.contains("entityReference"), "method entityReference");
        check(mn.contains("setTransformer"), "method setTransformer");
        check(mn.contains("getTransformer"), "method getTransformer");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("warning"), "method warning");
        check(mn.contains("fireStartEntity"), "method fireStartEntity");
        check(mn.contains("fireCDATAEvent"), "method fireCDATAEvent");
        check(mn.contains("fireCommentEvent"), "method fireCommentEvent");
        check(mn.contains("fireEndEntity"), "method fireEndEntity");
        check(mn.contains("fireStartDoc"), "method fireStartDoc");
        check(mn.contains("fireEndDoc"), "method fireEndDoc");
        check(mn.contains("fireStartElem"), "method fireStartElem");
        check(mn.contains("fireEscapingEvent"), "method fireEscapingEvent");
        check(mn.contains("fireEntityReference"), "method fireEntityReference");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startDocumentInternal"), "method startDocumentInternal");
        check(mn.contains("setDocumentInfo"), "method setDocumentInfo");
        check(mn.contains("setSourceLocator"), "method setSourceLocator");
        check(mn.contains("setNamespaceMappings"), "method setNamespaceMappings");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("inTemporaryOutputState"), "method inTemporaryOutputState");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("setDTDEntityExpansion"), "method setDTDEntityExpansion");
        check(mn.contains("initCdataElems"), "method initCdataElems");
        check(mn.contains("documentIsEmpty"), "method documentIsEmpty");
        check(mn.contains("isCdataSection"), "method isCdataSection");
        check(mn.contains("getOutputProperty"), "method getOutputProperty");
        check(mn.contains("getOutputPropertyNonDefault"), "method getOutputPropertyNonDefault");
        check(mn.contains("getOutputPropertyDefault"), "method getOutputPropertyDefault");
        check(mn.contains("setOutputProperty"), "method setOutputProperty");
        check(mn.contains("setOutputPropertyDefault"), "method setOutputPropertyDefault");
        check(mn.contains("getOutputPropDefaultKeys"), "method getOutputPropDefaultKeys");
        check(mn.contains("getOutputPropKeys"), "method getOutputPropKeys");
        check(mn.contains("setProp"), "method setProp");
        check(mn.contains("getFirstCharLocName"), "method getFirstCharLocName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
