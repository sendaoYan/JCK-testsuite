/*
 * @test
 * @summary sun.management.GarbageCollectionNotifInfoCompositeData conformance: class and members.
 * @run main GarbageCollectionNotifInfoCompositeDataTest
 */

public class GarbageCollectionNotifInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.GarbageCollectionNotifInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.GarbageCollectionNotifInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getBaseGcNotifInfoCompositeType"), "declares method getBaseGcNotifInfoCompositeType");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getCompositeTypeByBuilder"), "declares method getCompositeTypeByBuilder");
        check(mnames.contains("getGarbageCollectionNotifInfo"), "declares method getGarbageCollectionNotifInfo");
        check(mnames.contains("getGcAction"), "declares method getGcAction");
        check(mnames.contains("getGcCause"), "declares method getGcCause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
