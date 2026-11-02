/*
 * @test
 * @summary sun.misc.Unsafe conformance: class and members.
 * @run main UnsafeTest
 */

public class UnsafeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Unsafe", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Unsafe"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addressSize"), "declares method addressSize");
        check(mnames.contains("allocateInstance"), "declares method allocateInstance");
        check(mnames.contains("allocateMemory"), "declares method allocateMemory");
        check(mnames.contains("arrayBaseOffset"), "declares method arrayBaseOffset");
        check(mnames.contains("arrayIndexScale"), "declares method arrayIndexScale");
        check(mnames.contains("compareAndSwapInt"), "declares method compareAndSwapInt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
