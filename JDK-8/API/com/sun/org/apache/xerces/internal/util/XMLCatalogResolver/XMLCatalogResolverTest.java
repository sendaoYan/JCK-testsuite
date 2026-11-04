/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLCatalogResolver conformance: class hierarchy and members.
 * @run main XMLCatalogResolverTest
 */
public class XMLCatalogResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLCatalogResolver", false, XMLCatalogResolverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver");
        check(hasInterface(c, "org.xml.sax.ext.EntityResolver2"), "has interface org.xml.sax.ext.EntityResolver2");
        check(hasInterface(c, "org.w3c.dom.ls.LSResourceResolver"), "has interface org.w3c.dom.ls.LSResourceResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCatalogList"), "method getCatalogList");
        check(mn.contains("setCatalogList"), "method setCatalogList");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("getPreferPublic"), "method getPreferPublic");
        check(mn.contains("setPreferPublic"), "method setPreferPublic");
        check(mn.contains("getUseLiteralSystemId"), "method getUseLiteralSystemId");
        check(mn.contains("setUseLiteralSystemId"), "method setUseLiteralSystemId");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("getExternalSubset"), "method getExternalSubset");
        check(mn.contains("resolveResource"), "method resolveResource");
        check(mn.contains("resolveIdentifier"), "method resolveIdentifier");
        check(mn.contains("resolveSystem"), "method resolveSystem");
        check(mn.contains("resolvePublic"), "method resolvePublic");
        check(mn.contains("resolveURI"), "method resolveURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
