/*
 * @test
 * @summary sun.util.xml.PlatformXmlPropertiesProvider conformance: class and members.
 * @run main PlatformXmlPropertiesProviderTest
 */

public class PlatformXmlPropertiesProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.xml.PlatformXmlPropertiesProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.xml.PlatformXmlPropertiesProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.spi.XmlPropertiesProvider"), "superclass sun.util.spi.XmlPropertiesProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("emitDocument"), "declares method emitDocument");
        check(mnames.contains("getLoadingDoc"), "declares method getLoadingDoc");
        check(mnames.contains("importProperties"), "declares method importProperties");
        check(mnames.contains("load"), "declares method load");
        check(mnames.contains("store"), "declares method store");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
