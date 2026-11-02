/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.Catalog conformance: class hierarchy and members.
 * @run main CatalogTest
 */
public class CatalogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.Catalog", false, CatalogTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCatalogManager"), "method getCatalogManager");
        check(mn.contains("setCatalogManager"), "method setCatalogManager");
        check(mn.contains("setupReaders"), "method setupReaders");
        check(mn.contains("addReader"), "method addReader");
        check(mn.contains("copyReaders"), "method copyReaders");
        check(mn.contains("newCatalog"), "method newCatalog");
        check(mn.contains("getCurrentBase"), "method getCurrentBase");
        check(mn.contains("getDefaultOverride"), "method getDefaultOverride");
        check(mn.contains("loadSystemCatalogs"), "method loadSystemCatalogs");
        check(mn.contains("parseCatalog"), "method parseCatalog");
        check(mn.contains("parsePendingCatalogs"), "method parsePendingCatalogs");
        check(mn.contains("parseCatalogFile"), "method parseCatalogFile");
        check(mn.contains("addEntry"), "method addEntry");
        check(mn.contains("unknownEntry"), "method unknownEntry");
        check(mn.contains("parseAllCatalogs"), "method parseAllCatalogs");
        check(mn.contains("resolveDoctype"), "method resolveDoctype");
        check(mn.contains("resolveDocument"), "method resolveDocument");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("resolveNotation"), "method resolveNotation");
        check(mn.contains("resolvePublic"), "method resolvePublic");
        check(mn.contains("resolveLocalPublic"), "method resolveLocalPublic");
        check(mn.contains("resolveSystem"), "method resolveSystem");
        check(mn.contains("resolveLocalSystem"), "method resolveLocalSystem");
        check(mn.contains("resolveURI"), "method resolveURI");
        check(mn.contains("resolveLocalURI"), "method resolveLocalURI");
        check(mn.contains("resolveSubordinateCatalogs"), "method resolveSubordinateCatalogs");
        check(mn.contains("fixSlashes"), "method fixSlashes");
        check(mn.contains("makeAbsolute"), "method makeAbsolute");
        check(mn.contains("normalizeURI"), "method normalizeURI");
        check(mn.contains("encodedByte"), "method encodedByte");
        check(mn.contains("addDelegate"), "method addDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
