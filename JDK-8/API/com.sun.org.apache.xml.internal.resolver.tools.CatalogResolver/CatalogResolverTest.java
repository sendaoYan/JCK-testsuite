/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver conformance: class hierarchy and members.
 * @run main CatalogResolverTest
 */
public class CatalogResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver", false, CatalogResolverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "has interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "javax.xml.transform.URIResolver"), "has interface javax.xml.transform.URIResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCatalog"), "method getCatalog");
        check(mn.contains("getResolvedEntity"), "method getResolvedEntity");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("resolve"), "method resolve");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
