/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator conformance: class hierarchy and members.
 * @run main XMLSchemaValidatorTest
 */
public class XMLSchemaValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator", false, XMLSchemaValidatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.xs.identity.FieldActivator"), "has interface com.sun.org.apache.xerces.internal.impl.xs.identity.FieldActivator");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.RevalidationHandler"), "has interface com.sun.org.apache.xerces.internal.impl.RevalidationHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
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
        check(mn.contains("characterData"), "method characterData");
        check(mn.contains("elementDefault"), "method elementDefault");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("startValueScopeFor"), "method startValueScopeFor");
        check(mn.contains("activateField"), "method activateField");
        check(mn.contains("endValueScopeFor"), "method endValueScopeFor");
        check(mn.contains("setMayMatch"), "method setMayMatch");
        check(mn.contains("mayMatch"), "method mayMatch");
        check(mn.contains("ensureStackCapacity"), "method ensureStackCapacity");
        check(mn.contains("handleStartDocument"), "method handleStartDocument");
        check(mn.contains("handleEndDocument"), "method handleEndDocument");
        check(mn.contains("handleCharacters"), "method handleCharacters");
        check(mn.contains("handleIgnorableWhitespace"), "method handleIgnorableWhitespace");
        check(mn.contains("handleStartElement"), "method handleStartElement");
        check(mn.contains("handleEndElement"), "method handleEndElement");
        check(mn.contains("endElementPSVI"), "method endElementPSVI");
        check(mn.contains("getEmptyAugs"), "method getEmptyAugs");
        check(mn.contains("storeLocations"), "method storeLocations");
        check(mn.contains("findSchemaGrammar"), "method findSchemaGrammar");
        check(mn.contains("getAndCheckXsiType"), "method getAndCheckXsiType");
        check(mn.contains("getXsiNil"), "method getXsiNil");
        check(mn.contains("processAttributes"), "method processAttributes");
        check(mn.contains("processOneAttribute"), "method processOneAttribute");
        check(mn.contains("addDefaultAttributes"), "method addDefaultAttributes");
        check(mn.contains("processElementContent"), "method processElementContent");
        check(mn.contains("elementLocallyValidType"), "method elementLocallyValidType");
        check(mn.contains("elementLocallyValidComplexType"), "method elementLocallyValidComplexType");
        check(mn.contains("reportSchemaError"), "method reportSchemaError");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
