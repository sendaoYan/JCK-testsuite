/*
 * @test
 * @summary sun.misc.Cleaner conformance: class and members.
 * @run main CleanerTest
 */

public class CleanerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Cleaner", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Cleaner"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.ref.PhantomReference"), "superclass java.lang.ref.PhantomReference");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("clean"), "declares method clean");
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("remove"), "declares method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
