/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.readers.TextCatalogReader conformance: class hierarchy and members.
 * @run main TextCatalogReaderTest
 */
public class TextCatalogReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.readers.TextCatalogReader", false, TextCatalogReaderTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.resolver.readers.CatalogReader"), "has interface com.sun.org.apache.xml.internal.resolver.readers.CatalogReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setCaseSensitive"), "method setCaseSensitive");
        check(mn.contains("getCaseSensitive"), "method getCaseSensitive");
        check(mn.contains("readCatalog"), "method readCatalog");
        check(mn.contains("finalize"), "method finalize");
        check(mn.contains("nextToken"), "method nextToken");
        check(mn.contains("nextChar"), "method nextChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
