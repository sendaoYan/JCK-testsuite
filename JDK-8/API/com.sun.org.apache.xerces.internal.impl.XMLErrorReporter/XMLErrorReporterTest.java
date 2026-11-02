/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLErrorReporter conformance: class hierarchy and members.
 * @run main XMLErrorReporterTest
 */
public class XMLErrorReporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLErrorReporter", false, XMLErrorReporterTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setLocale"), "method setLocale");
        check(mn.contains("getLocale"), "method getLocale");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("putMessageFormatter"), "method putMessageFormatter");
        check(mn.contains("getMessageFormatter"), "method getMessageFormatter");
        check(mn.contains("removeMessageFormatter"), "method removeMessageFormatter");
        check(mn.contains("reportError"), "method reportError");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("getSAXErrorHandler"), "method getSAXErrorHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
