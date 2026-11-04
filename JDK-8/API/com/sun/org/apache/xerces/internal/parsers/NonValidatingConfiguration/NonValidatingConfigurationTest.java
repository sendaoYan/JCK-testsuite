/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.NonValidatingConfiguration conformance: class hierarchy and members.
 * @run main NonValidatingConfigurationTest
 */
public class NonValidatingConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.NonValidatingConfiguration", false, NonValidatingConfigurationTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration"), "superclass com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getPropertyState"), "method getPropertyState");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getFeatureState"), "method getFeatureState");
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("cleanup"), "method cleanup");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("configurePipeline"), "method configurePipeline");
        check(mn.contains("checkFeature"), "method checkFeature");
        check(mn.contains("checkProperty"), "method checkProperty");
        check(mn.contains("createEntityManager"), "method createEntityManager");
        check(mn.contains("createErrorReporter"), "method createErrorReporter");
        check(mn.contains("createDocumentScanner"), "method createDocumentScanner");
        check(mn.contains("createDTDScanner"), "method createDTDScanner");
        check(mn.contains("createDatatypeValidatorFactory"), "method createDatatypeValidatorFactory");
        check(mn.contains("createValidationManager"), "method createValidationManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
