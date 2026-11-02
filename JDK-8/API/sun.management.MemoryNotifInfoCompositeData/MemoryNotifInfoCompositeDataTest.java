/*
 * @test
 * @summary sun.management.MemoryNotifInfoCompositeData conformance: class and members.
 * @run main MemoryNotifInfoCompositeDataTest
 */

public class MemoryNotifInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.MemoryNotifInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.MemoryNotifInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getCount"), "declares method getCount");
        check(mnames.contains("getMemoryNotifInfo"), "declares method getMemoryNotifInfo");
        check(mnames.contains("getPoolName"), "declares method getPoolName");
        check(mnames.contains("getUsage"), "declares method getUsage");
        check(mnames.contains("toCompositeData"), "declares method toCompositeData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
