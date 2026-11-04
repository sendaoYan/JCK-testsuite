/*
 * @test
 * @summary sun.management.GcInfoCompositeData conformance: class and members.
 * @run main GcInfoCompositeDataTest
 */

public class GcInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.GcInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.GcInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("cast"), "declares method cast");
        check(mnames.contains("getBaseGcInfoCompositeType"), "declares method getBaseGcInfoCompositeType");
        check(mnames.contains("getBaseGcInfoItemNames"), "declares method getBaseGcInfoItemNames");
        check(mnames.contains("getBaseGcInfoItemTypes"), "declares method getBaseGcInfoItemTypes");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getEndTime"), "declares method getEndTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
