/*
 * @test
 * @summary sun.nio.ch.SimpleAsynchronousFileChannelImpl conformance: class and members.
 * @run main SimpleAsynchronousFileChannelImplTest
 */

public class SimpleAsynchronousFileChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SimpleAsynchronousFileChannelImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.SimpleAsynchronousFileChannelImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.AsynchronousFileChannelImpl"), "superclass sun.nio.ch.AsynchronousFileChannelImpl");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("force"), "declares method force");
        check(mnames.contains("implLock"), "declares method implLock");
        check(mnames.contains("implRead"), "declares method implRead");
        check(mnames.contains("implRelease"), "declares method implRelease");
        check(mnames.contains("implWrite"), "declares method implWrite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
