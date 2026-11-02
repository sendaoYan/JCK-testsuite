/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl conformance: class hierarchy and members.
 * @run main XMLDocumentScannerImplTest
 */
public class XMLDocumentScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl", false, XMLDocumentScannerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl"), "superclass com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("getScannetState"), "method getScannetState");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("getDTDDecl"), "method getDTDDecl");
        check(mn.contains("getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(mn.contains("next"), "method next");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("createContentDriver"), "method createContentDriver");
        check(mn.contains("scanDoctypeDecl"), "method scanDoctypeDecl");
        check(mn.contains("setEndDTDScanState"), "method setEndDTDScanState");
        check(mn.contains("getScannerStateName"), "method getScannerStateName");
        check(mn.contains("refresh"), "method refresh");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        check(mn.contains("access$102"), "method access$102");
        check(mn.contains("access$200"), "method access$200");
        check(mn.contains("access$300"), "method access$300");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
