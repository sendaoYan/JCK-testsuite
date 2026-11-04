/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl conformance: class hierarchy and members.
 * @run main XMLDocumentFragmentScannerImplTest
 */
public class XMLDocumentFragmentScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl", false, XMLDocumentFragmentScannerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLScanner"), "superclass com.sun.org.apache.xerces.internal.impl.XMLScanner");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.XMLEntityHandler"), "has interface com.sun.org.apache.xerces.internal.impl.XMLEntityHandler");
        check(hasInterface(c, "com.sun.xml.internal.stream.XMLBufferListener"), "has interface com.sun.xml.internal.stream.XMLBufferListener");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("scanDocument"), "method scanDocument");
        check(mn.contains("getElementQName"), "method getElementQName");
        check(mn.contains("next"), "method next");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("resetCommon"), "method resetCommon");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("createContentDriver"), "method createContentDriver");
        check(mn.contains("scanXMLDeclOrTextDecl"), "method scanXMLDeclOrTextDecl");
        check(mn.contains("getPITarget"), "method getPITarget");
        check(mn.contains("getPIData"), "method getPIData");
        check(mn.contains("getCharacterData"), "method getCharacterData");
        check(mn.contains("scanPIData"), "method scanPIData");
        check(mn.contains("scanComment"), "method scanComment");
        check(mn.contains("getComment"), "method getComment");
        check(mn.contains("addElement"), "method addElement");
        check(mn.contains("resetPointer"), "method resetPointer");
        check(mn.contains("storePointerForADepth"), "method storePointerForADepth");
        check(mn.contains("canStore"), "method canStore");
        check(mn.contains("getElementPointer"), "method getElementPointer");
        check(mn.contains("skipFromTheBuffer"), "method skipFromTheBuffer");
        check(mn.contains("skipQElement"), "method skipQElement");
        check(mn.contains("skipElement"), "method skipElement");
        check(mn.contains("scanStartElement"), "method scanStartElement");
        check(mn.contains("seekCloseOfStartTag"), "method seekCloseOfStartTag");
        check(mn.contains("hasAttributes"), "method hasAttributes");
        check(mn.contains("getAttributeIterator"), "method getAttributeIterator");
        check(mn.contains("standaloneSet"), "method standaloneSet");
        check(mn.contains("isStandAlone"), "method isStandAlone");
        check(mn.contains("scanAttribute"), "method scanAttribute");
        check(mn.contains("scanContent"), "method scanContent");
        check(mn.contains("scanCDATASection"), "method scanCDATASection");
        check(mn.contains("scanEndElement"), "method scanEndElement");
        check(mn.contains("scanCharReference"), "method scanCharReference");
        check(mn.contains("scanEntityReference"), "method scanEntityReference");
        check(mn.contains("checkDepth"), "method checkDepth");
        check(mn.contains("setScannerState"), "method setScannerState");
        check(mn.contains("setDriver"), "method setDriver");
        check(mn.contains("getScannerStateName"), "method getScannerStateName");
        check(mn.contains("getEntityName"), "method getEntityName");
        check(mn.contains("getDriverName"), "method getDriverName");
        check(mn.contains("checkAccess"), "method checkAccess");
        check(mn.contains("pr"), "method pr");
        check(mn.contains("getString"), "method getString");
        check(mn.contains("refresh"), "method refresh");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
