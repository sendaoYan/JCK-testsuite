/*
 * @test
 * @summary sun.management.LazyCompositeData conformance: abstract class and members.
 * @run main LazyCompositeDataTest
 */

public class LazyCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.LazyCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.LazyCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("javax.management.openmbean.CompositeData"), "interface javax.management.openmbean.CompositeData");
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("compositeData"), "declares method compositeData");
        check(mnames.contains("containsKey"), "declares method containsKey");
        check(mnames.contains("containsValue"), "declares method containsValue");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getAll"), "declares method getAll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
