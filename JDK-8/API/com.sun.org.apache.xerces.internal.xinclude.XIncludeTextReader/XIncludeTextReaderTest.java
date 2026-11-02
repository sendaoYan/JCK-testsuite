/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xinclude.XIncludeTextReader conformance: class hierarchy and members.
 * @run main XIncludeTextReaderTest
 */
public class XIncludeTextReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xinclude.XIncludeTextReader", false, XIncludeTextReaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setErrorReporter"), "method setErrorReporter");
        check(mn.contains("getReader"), "method getReader");
        check(mn.contains("getEncodingName"), "method getEncodingName");
        check(mn.contains("consumeBOM"), "method consumeBOM");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("close"), "method close");
        check(mn.contains("isValid"), "method isValid");
        check(mn.contains("setBufferSize"), "method setBufferSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
