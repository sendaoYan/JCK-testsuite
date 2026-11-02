/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor conformance: class hierarchy and members.
 * @run main XMLDTDProcessorTest
 */
public class XMLDTDProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor", false, XMLDTDProcessorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelFilter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setDTDContentModelHandler"), "method setDTDContentModelHandler");
        check(mn.contains("getDTDContentModelHandler"), "method getDTDContentModelHandler");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("checkStandaloneEntityRef"), "method checkStandaloneEntityRef");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
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
        check(mn.contains("setDTDSource"), "method setDTDSource");
        check(mn.contains("getDTDSource"), "method getDTDSource");
        check(mn.contains("setDTDContentModelSource"), "method setDTDContentModelSource");
        check(mn.contains("getDTDContentModelSource"), "method getDTDContentModelSource");
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("any"), "method any");
        check(mn.contains("empty"), "method empty");
        check(mn.contains("startGroup"), "method startGroup");
        check(mn.contains("pcdata"), "method pcdata");
        check(mn.contains("element"), "method element");
        check(mn.contains("separator"), "method separator");
        check(mn.contains("occurrence"), "method occurrence");
        check(mn.contains("endGroup"), "method endGroup");
        check(mn.contains("endContentModel"), "method endContentModel");
        check(mn.contains("isValidNmtoken"), "method isValidNmtoken");
        check(mn.contains("isValidName"), "method isValidName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
