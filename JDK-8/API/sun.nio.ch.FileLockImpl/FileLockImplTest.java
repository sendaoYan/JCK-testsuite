/*
 * @test
 * @summary sun.nio.ch.FileLockImpl conformance: class and members.
 * @run main FileLockImplTest
 */

public class FileLockImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.FileLockImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.FileLockImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.FileLock"), "superclass java.nio.channels.FileLock");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("invalidate"), "declares method invalidate");
        check(mnames.contains("isValid"), "declares method isValid");
        check(mnames.contains("release"), "declares method release");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
