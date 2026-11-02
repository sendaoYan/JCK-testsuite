/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator conformance: class hierarchy and members.
 * @run main XMLDTDValidatorTest
 */
public class XMLDTDValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator", false, XMLDTDValidatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter"), "has interface com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.RevalidationHandler"), "has interface com.sun.org.apache.xerces.internal.impl.RevalidationHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarBucket"), "method getGrammarBucket");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("setDocumentSource"), "method setDocumentSource");
        check(mn.contains("getDocumentSource"), "method getDocumentSource");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("hasGrammar"), "method hasGrammar");
        check(mn.contains("validate"), "method validate");
        check(mn.contains("addDTDDefaultAttrsAndValidate"), "method addDTDDefaultAttrsAndValidate");
        check(mn.contains("getExternalEntityRefInAttrValue"), "method getExternalEntityRefInAttrValue");
        check(mn.contains("validateDTDattribute"), "method validateDTDattribute");
        check(mn.contains("invalidStandaloneAttDef"), "method invalidStandaloneAttDef");
        check(mn.contains("init"), "method init");
        check(mn.contains("handleStartElement"), "method handleStartElement");
        check(mn.contains("startNamespaceScope"), "method startNamespaceScope");
        check(mn.contains("handleEndElement"), "method handleEndElement");
        check(mn.contains("endNamespaceScope"), "method endNamespaceScope");
        check(mn.contains("isSpace"), "method isSpace");
        check(mn.contains("characterData"), "method characterData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
