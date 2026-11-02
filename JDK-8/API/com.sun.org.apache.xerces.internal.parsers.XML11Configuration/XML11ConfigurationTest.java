/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.XML11Configuration conformance: class hierarchy and members.
 * @run main XML11ConfigurationTest
 */
public class XML11ConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.XML11Configuration", false, XML11ConfigurationTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings"), "superclass com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.parsers.XML11Configurable"), "has interface com.sun.org.apache.xerces.internal.parsers.XML11Configurable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("getDocumentHandler"), "method getDocumentHandler");
        check(mn.contains("setDTDHandler"), "method setDTDHandler");
        check(mn.contains("getDTDHandler"), "method getDTDHandler");
        check(mn.contains("setDTDContentModelHandler"), "method setDTDContentModelHandler");
        check(mn.contains("getDTDContentModelHandler"), "method getDTDContentModelHandler");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("cleanup"), "method cleanup");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("getFeatureState"), "method getFeatureState");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getPropertyState"), "method getPropertyState");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("resetCommon"), "method resetCommon");
        check(mn.contains("resetXML11"), "method resetXML11");
        check(mn.contains("configureXML11Pipeline"), "method configureXML11Pipeline");
        check(mn.contains("configurePipeline"), "method configurePipeline");
        check(mn.contains("checkFeature"), "method checkFeature");
        check(mn.contains("checkProperty"), "method checkProperty");
        check(mn.contains("addComponent"), "method addComponent");
        check(mn.contains("addCommonComponent"), "method addCommonComponent");
        check(mn.contains("addXML11Component"), "method addXML11Component");
        check(mn.contains("addRecognizedParamsAndSetDefaults"), "method addRecognizedParamsAndSetDefaults");
        check(mn.contains("getFeatureState0"), "method getFeatureState0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
