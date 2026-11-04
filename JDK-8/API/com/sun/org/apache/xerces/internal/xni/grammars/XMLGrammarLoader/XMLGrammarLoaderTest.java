/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader conformance: interface hierarchy and members.
 * @run main XMLGrammarLoaderTest
 */
public class XMLGrammarLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader", false, XMLGrammarLoaderTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("loadGrammar"), "method loadGrammar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
