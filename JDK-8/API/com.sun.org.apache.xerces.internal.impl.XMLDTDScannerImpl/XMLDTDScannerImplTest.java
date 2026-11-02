/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl conformance: class hierarchy and members.
 * @run main XMLDTDScannerImplTest
 */
public class XMLDTDScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl", false, XMLDTDScannerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLScanner"), "superclass com.sun.org.apache.xerces.internal.impl.XMLScanner");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.XMLEntityHandler"), "has interface com.sun.org.apache.xerces.internal.impl.XMLEntityHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("setLimitAnalyzer"), "method setLimitAnalyzer");
        check(mn.contains("scanDTDExternalSubset"), "method scanDTDExternalSubset");
        check(mn.contains("scanDTDInternalSubset"), "method scanDTDInternalSubset");
        check(mn.contains("skipDTD"), "method skipDTD");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setDTDContentModelHandler"), "method setDTDContentModelHandler");
        check(mn.contains("getDTDContentModelHandler"), "method getDTDContentModelHandler");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("setScannerState"), "method setScannerState");
        check(mn.contains("scanningInternalSubset"), "method scanningInternalSubset");
        check(mn.contains("startPE"), "method startPE");
        check(mn.contains("scanTextDecl"), "method scanTextDecl");
        check(mn.contains("scanPIData"), "method scanPIData");
        check(mn.contains("scanComment"), "method scanComment");
        check(mn.contains("scanElementDecl"), "method scanElementDecl");
        check(mn.contains("scanAttlistDecl"), "method scanAttlistDecl");
        check(mn.contains("scanAttDefaultDecl"), "method scanAttDefaultDecl");
        check(mn.contains("scanEntityValue"), "method scanEntityValue");
        check(mn.contains("scanDecls"), "method scanDecls");
        check(mn.contains("getGrammar"), "method getGrammar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
