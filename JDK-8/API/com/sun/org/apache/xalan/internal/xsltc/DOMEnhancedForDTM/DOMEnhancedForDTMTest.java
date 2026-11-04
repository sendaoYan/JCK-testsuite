/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM conformance: reflection.
 * @run main DOMEnhancedForDTMTest
 */

public class DOMEnhancedForDTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM", false, DOMEnhancedForDTMTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DOMEnhancedForDTM"), "simple name DOMEnhancedForDTM");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.DOM"), "interface com.sun.org.apache.xalan.internal.xsltc.DOM");
        check(has(c, "getMapping"), "method getMapping");
        check(has(c, "getReverseMapping"), "method getReverseMapping");
        check(has(c, "getNamespaceMapping"), "method getNamespaceMapping");
        check(has(c, "getReverseNamespaceMapping"), "method getReverseNamespaceMapping");
        check(has(c, "getDocumentURI"), "method getDocumentURI");
        check(has(c, "setDocumentURI"), "method setDocumentURI");
        check(has(c, "getExpandedTypeID2"), "method getExpandedTypeID2");
        check(has(c, "hasDOMSource"), "method hasDOMSource");
        check(has(c, "getElementById"), "method getElementById");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
