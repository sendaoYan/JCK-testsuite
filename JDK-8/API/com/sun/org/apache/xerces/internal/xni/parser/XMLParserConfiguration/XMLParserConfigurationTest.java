/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration conformance: interface hierarchy and members.
 * @run main XMLParserConfigurationTest
 */
public class XMLParserConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration", false, XMLParserConfigurationTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("parse"), "method parse");
        check(mn.contains("addRecognizedFeatures"), "method addRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("addRecognizedProperties"), "method addRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setDTDContentModelHandler"), "method setDTDContentModelHandler");
        check(mn.contains("getDTDContentModelHandler"), "method getDTDContentModelHandler");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
