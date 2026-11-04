/*
 * @test
 * @summary sun.misc.LRUCache conformance: abstract class and members.
 * @run main LRUCacheTest
 */

public class LRUCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.LRUCache", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.LRUCache"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("forName"), "declares method forName");
        check(mnames.contains("hasName"), "declares method hasName");
        check(mnames.contains("moveToFront"), "declares method moveToFront");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
