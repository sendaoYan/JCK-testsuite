/*
 * @test
 * @summary com.sun.org.apache.xml.internal.resolver.Resolver conformance: class hierarchy and members.
 * @run main ResolverTest
 */
public class ResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.resolver.Resolver", false, ResolverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.resolver.Catalog"), "superclass com.sun.org.apache.xml.internal.resolver.Catalog");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setupReaders"), "method setupReaders");
        check(mn.contains("addEntry"), "method addEntry");
        check(mn.contains("resolveURI"), "method resolveURI");
        check(mn.contains("resolveSystem"), "method resolveSystem");
        check(mn.contains("resolvePublic"), "method resolvePublic");
        check(mn.contains("resolveExternalSystem"), "method resolveExternalSystem");
        check(mn.contains("resolveExternalPublic"), "method resolveExternalPublic");
        check(mn.contains("queryResolver"), "method queryResolver");
        check(mn.contains("resolveAllSystemReverse"), "method resolveAllSystemReverse");
        check(mn.contains("resolveSystemReverse"), "method resolveSystemReverse");
        check(mn.contains("resolveAllSystem"), "method resolveAllSystem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
