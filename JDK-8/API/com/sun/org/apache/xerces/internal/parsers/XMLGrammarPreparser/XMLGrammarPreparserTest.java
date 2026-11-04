/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser conformance: class hierarchy and members.
 * @run main XMLGrammarPreparserTest
 */
public class XMLGrammarPreparserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser", false, XMLGrammarPreparserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("registerPreparser"), "method registerPreparser");
        check(mn.contains("preparseGrammar"), "method preparseGrammar");
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("setGrammarPool"), "method setGrammarPool");
        check(mn.contains("getGrammarPool"), "method getGrammarPool");
        check(mn.contains("getLoader"), "method getLoader");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getProperty"), "method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
