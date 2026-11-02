/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.CatalogManager conformance: class hierarchy and members.
 * @run main CatalogManagerTest
 */
public class CatalogManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.CatalogManager", false, CatalogManagerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setBootstrapResolver"), "method setBootstrapResolver");
        check(mn.contains("getBootstrapResolver"), "method getBootstrapResolver");
        check(mn.contains("getStaticManager"), "method getStaticManager");
        check(mn.contains("getIgnoreMissingProperties"), "method getIgnoreMissingProperties");
        check(mn.contains("setIgnoreMissingProperties"), "method setIgnoreMissingProperties");
        check(mn.contains("ignoreMissingProperties"), "method ignoreMissingProperties");
        check(mn.contains("getVerbosity"), "method getVerbosity");
        check(mn.contains("setVerbosity"), "method setVerbosity");
        check(mn.contains("verbosity"), "method verbosity");
        check(mn.contains("getRelativeCatalogs"), "method getRelativeCatalogs");
        check(mn.contains("setRelativeCatalogs"), "method setRelativeCatalogs");
        check(mn.contains("relativeCatalogs"), "method relativeCatalogs");
        check(mn.contains("getCatalogFiles"), "method getCatalogFiles");
        check(mn.contains("setCatalogFiles"), "method setCatalogFiles");
        check(mn.contains("catalogFiles"), "method catalogFiles");
        check(mn.contains("getPreferPublic"), "method getPreferPublic");
        check(mn.contains("setPreferPublic"), "method setPreferPublic");
        check(mn.contains("preferPublic"), "method preferPublic");
        check(mn.contains("getUseStaticCatalog"), "method getUseStaticCatalog");
        check(mn.contains("setUseStaticCatalog"), "method setUseStaticCatalog");
        check(mn.contains("staticCatalog"), "method staticCatalog");
        check(mn.contains("getPrivateCatalog"), "method getPrivateCatalog");
        check(mn.contains("getCatalog"), "method getCatalog");
        check(mn.contains("queryAllowOasisXMLCatalogPI"), "method queryAllowOasisXMLCatalogPI");
        check(mn.contains("getAllowOasisXMLCatalogPI"), "method getAllowOasisXMLCatalogPI");
        check(mn.contains("overrideDefaultParser"), "method overrideDefaultParser");
        check(mn.contains("setAllowOasisXMLCatalogPI"), "method setAllowOasisXMLCatalogPI");
        check(mn.contains("allowOasisXMLCatalogPI"), "method allowOasisXMLCatalogPI");
        check(mn.contains("queryCatalogClassName"), "method queryCatalogClassName");
        check(mn.contains("getCatalogClassName"), "method getCatalogClassName");
        check(mn.contains("setCatalogClassName"), "method setCatalogClassName");
        check(mn.contains("catalogClassName"), "method catalogClassName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
