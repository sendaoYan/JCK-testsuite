/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration conformance: abstract class hierarchy and members.
 * @run main BasicParserConfigurationTest
 */
public class BasicParserConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration", false, BasicParserConfigurationTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings"), "superclass com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addComponent"), "method addComponent");
        check(mn.contains("parse"), "method parse");
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
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("checkProperty"), "method checkProperty");
        check(mn.contains("checkFeature"), "method checkFeature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
