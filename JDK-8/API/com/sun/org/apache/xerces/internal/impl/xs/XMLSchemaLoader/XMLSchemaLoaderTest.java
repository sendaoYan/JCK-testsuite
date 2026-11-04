/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader conformance: class hierarchy and members.
 * @run main XMLSchemaLoaderTest
 */
public class XMLSchemaLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader", false, XMLSchemaLoaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSLoader"), "has interface com.sun.org.apache.xerces.internal.xs.XSLoader");
        check(hasInterface(c, "org.w3c.dom.DOMConfiguration"), "has interface org.w3c.dom.DOMConfiguration");
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
        check(mn.contains("loadSchema"), "method loadSchema");
        check(mn.contains("resolveDocument"), "method resolveDocument");
        check(mn.contains("processExternalHints"), "method processExternalHints");
        check(mn.contains("tokenizeSchemaLocationStr"), "method tokenizeSchemaLocationStr");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getConfig"), "method getConfig");
        check(mn.contains("load"), "method load");
        check(mn.contains("loadInputList"), "method loadInputList");
        check(mn.contains("loadURI"), "method loadURI");
        check(mn.contains("loadURIList"), "method loadURIList");
        check(mn.contains("reportDOMFatalError"), "method reportDOMFatalError");
        check(mn.contains("canSetParameter"), "method canSetParameter");
        check(mn.contains("getParameter"), "method getParameter");
        check(mn.contains("getParameterNames"), "method getParameterNames");
        check(mn.contains("setParameter"), "method setParameter");
        check(mn.contains("dom2xmlInputSource"), "method dom2xmlInputSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
