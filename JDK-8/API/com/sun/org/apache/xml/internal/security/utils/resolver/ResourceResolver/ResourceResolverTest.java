/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolver conformance: class hierarchy and members.
 * @run main ResourceResolverTest
 */
public class ResourceResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolver", false, ResourceResolverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("register"), "method register");
        check(mn.contains("registerAtStart"), "method registerAtStart");
        check(mn.contains("registerDefaultResolvers"), "method registerDefaultResolvers");
        check(mn.contains("resolve"), "method resolve");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("addProperties"), "method addProperties");
        check(mn.contains("getPropertyKeys"), "method getPropertyKeys");
        check(mn.contains("understandsProperty"), "method understandsProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
