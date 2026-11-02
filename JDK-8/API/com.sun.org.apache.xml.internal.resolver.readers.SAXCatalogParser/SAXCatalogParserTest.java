/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser conformance: interface hierarchy and members.
 * @run main SAXCatalogParserTest
 */
public class SAXCatalogParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser", false, SAXCatalogParserTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "has interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "has interface org.xml.sax.DocumentHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setCatalog"), "method setCatalog");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
