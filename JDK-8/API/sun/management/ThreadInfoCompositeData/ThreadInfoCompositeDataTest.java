/*
 * @test
 * @summary sun.management.ThreadInfoCompositeData conformance: class and members.
 * @run main ThreadInfoCompositeDataTest
 */

public class ThreadInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.ThreadInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.ThreadInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("blockedCount"), "declares method blockedCount");
        check(mnames.contains("blockedTime"), "declares method blockedTime");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getInstance"), "declares method getInstance");
        check(mnames.contains("getThreadInfo"), "declares method getThreadInfo");
        check(mnames.contains("inNative"), "declares method inNative");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
