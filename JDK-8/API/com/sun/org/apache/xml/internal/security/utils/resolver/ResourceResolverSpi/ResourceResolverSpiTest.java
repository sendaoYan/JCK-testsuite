/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi conformance: abstract class hierarchy and members.
 * @run main ResourceResolverSpiTest
 */
public class ResourceResolverSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi", false, ResourceResolverSpiTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineResolveURI"), "method engineResolveURI");
        check(mn.contains("engineSetProperty"), "method engineSetProperty");
        check(mn.contains("engineGetProperty"), "method engineGetProperty");
        check(mn.contains("engineAddProperies"), "method engineAddProperies");
        check(mn.contains("engineIsThreadSafe"), "method engineIsThreadSafe");
        check(mn.contains("engineCanResolveURI"), "method engineCanResolveURI");
        check(mn.contains("engineGetPropertyKeys"), "method engineGetPropertyKeys");
        check(mn.contains("understandsProperty"), "method understandsProperty");
        check(mn.contains("fixURI"), "method fixURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
