/*
 * @test
 * @summary sun.management.LockInfoCompositeData conformance: class and members.
 * @run main LockInfoCompositeDataTest
 */

public class LockInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.LockInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.LockInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getLockInfo"), "declares method getLockInfo");
        check(mnames.contains("getLockInfoCompositeType"), "declares method getLockInfoCompositeType");
        check(mnames.contains("toCompositeData"), "declares method toCompositeData");
        check(mnames.contains("toLockInfo"), "declares method toLockInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
