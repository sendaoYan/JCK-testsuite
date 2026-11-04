/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.DocumentCache.CachedDocument conformance: reflection.
 * @run main CachedDocumentTest
 */

public class CachedDocumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.DocumentCache$CachedDocument", false, CachedDocumentTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CachedDocument"), "simple name CachedDocument");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "loadDocument"), "method loadDocument");
        check(has(c, "getDocument"), "method getDocument");
        check(has(c, "getFirstReferenced"), "method getFirstReferenced");
        check(has(c, "getLastReferenced"), "method getLastReferenced");
        check(has(c, "getAccessCount"), "method getAccessCount");
        check(has(c, "incAccessCount"), "method incAccessCount");
        check(has(c, "getLastModified"), "method getLastModified");
        check(has(c, "setLastModified"), "method setLastModified");
        check(has(c, "getLatency"), "method getLatency");
        check(has(c, "getLastChecked"), "method getLastChecked");
        check(has(c, "setLastChecked"), "method setLastChecked");
        check(has(c, "getEstimatedSize"), "method getEstimatedSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
