/*
 * @test
 * @summary sun.misc.Lock conformance: class and members.
 * @run main LockTest
 */

public class LockTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Lock", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Lock"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("lock"), "declares method lock");
        check(mnames.contains("unlock"), "declares method unlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
