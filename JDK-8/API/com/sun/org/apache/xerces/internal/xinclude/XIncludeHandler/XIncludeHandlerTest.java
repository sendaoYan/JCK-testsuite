/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler conformance: class hierarchy and members.
 * @run main XIncludeHandlerTest
 */
public class XIncludeHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler", false, XIncludeHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("xmlDecl"), "method xmlDecl");
        check(mn.contains("doctypeDecl"), "method doctypeDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startGeneralEntity"), "method startGeneralEntity");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("endGeneralEntity"), "method endGeneralEntity");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("setDocumentSource"), "method setDocumentSource");
        check(mn.contains("getDocumentSource"), "method getDocumentSource");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("getDTDSource"), "method getDTDSource");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("setDTDSource"), "method setDTDSource");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("handleFallbackElement"), "method handleFallbackElement");
        check(mn.contains("handleIncludeElement"), "method handleIncludeElement");
        check(mn.contains("hasXIncludeNamespace"), "method hasXIncludeNamespace");
        check(mn.contains("isIncludeElement"), "method isIncludeElement");
        check(mn.contains("isFallbackElement"), "method isFallbackElement");
        check(mn.contains("sameBaseURIAsIncludeParent"), "method sameBaseURIAsIncludeParent");
        check(mn.contains("sameLanguageAsIncludeParent"), "method sameLanguageAsIncludeParent");
        check(mn.contains("searchForRecursiveIncludes"), "method searchForRecursiveIncludes");
        check(mn.contains("isTopLevelIncludedItem"), "method isTopLevelIncludedItem");
        check(mn.contains("isTopLevelIncludedItemViaInclude"), "method isTopLevelIncludedItemViaInclude");
        check(mn.contains("isTopLevelIncludedItemViaFallback"), "method isTopLevelIncludedItemViaFallback");
        check(mn.contains("processAttributes"), "method processAttributes");
        check(mn.contains("getRelativeBaseURI"), "method getRelativeBaseURI");
        check(mn.contains("modifyAugmentations"), "method modifyAugmentations");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("setState"), "method setState");
        check(mn.contains("setSawFallback"), "method setSawFallback");
        check(mn.contains("getSawFallback"), "method getSawFallback");
        check(mn.contains("setSawInclude"), "method setSawInclude");
        check(mn.contains("getSawInclude"), "method getSawInclude");
        check(mn.contains("reportResourceError"), "method reportResourceError");
        check(mn.contains("reportFatalError"), "method reportFatalError");
        check(mn.contains("setParent"), "method setParent");
        check(mn.contains("isRootDocument"), "method isRootDocument");
        check(mn.contains("addUnparsedEntity"), "method addUnparsedEntity");
        check(mn.contains("addNotation"), "method addNotation");
        check(mn.contains("checkUnparsedEntity"), "method checkUnparsedEntity");
        check(mn.contains("checkNotation"), "method checkNotation");
        check(mn.contains("checkAndSendUnparsedEntity"), "method checkAndSendUnparsedEntity");
        check(mn.contains("checkAndSendNotation"), "method checkAndSendNotation");
        check(mn.contains("copyFeatures"), "method copyFeatures");
        check(mn.contains("saveBaseURI"), "method saveBaseURI");
        check(mn.contains("restoreBaseURI"), "method restoreBaseURI");
        check(mn.contains("saveLanguage"), "method saveLanguage");
        check(mn.contains("restoreLanguage"), "method restoreLanguage");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("getLanguage"), "method getLanguage");
        check(mn.contains("getRelativeURI"), "method getRelativeURI");
        check(mn.contains("processXMLBaseAttributes"), "method processXMLBaseAttributes");
        check(mn.contains("processXMLLangAttributes"), "method processXMLLangAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
