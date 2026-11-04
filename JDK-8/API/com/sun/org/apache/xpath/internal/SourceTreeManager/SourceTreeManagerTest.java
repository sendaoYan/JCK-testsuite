/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.SourceTreeManager conformance: reflection.
 * @run main SourceTreeManagerTest
 */

public class SourceTreeManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.SourceTreeManager", false, SourceTreeManagerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SourceTreeManager"), "simple name SourceTreeManager");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "reset"), "method reset");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "getURIResolver"), "method getURIResolver");
        check(has(c, "findURIFromDoc"), "method findURIFromDoc");
        check(has(c, "resolveURI"), "method resolveURI");
        check(has(c, "removeDocumentFromCache"), "method removeDocumentFromCache");
        check(has(c, "putDocumentInCache"), "method putDocumentInCache");
        check(has(c, "getNode"), "method getNode");
        check(has(c, "getSourceTree"), "method getSourceTree");
        check(has(c, "parseToNode"), "method parseToNode");
        check(has(c, "getXMLReader"), "method getXMLReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
