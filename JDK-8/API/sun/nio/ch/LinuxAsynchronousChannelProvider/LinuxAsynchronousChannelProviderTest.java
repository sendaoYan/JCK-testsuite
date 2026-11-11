/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.nio.ch.LinuxAsynchronousChannelProvider conformance: class and members.
 * @run main LinuxAsynchronousChannelProviderTest
 */

public class LinuxAsynchronousChannelProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.LinuxAsynchronousChannelProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.LinuxAsynchronousChannelProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AsynchronousChannelProvider"), "superclass java.nio.channels.spi.AsynchronousChannelProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("defaultEventPort"), "declares method defaultEventPort");
        check(mnames.contains("openAsynchronousChannelGroup"), "declares method openAsynchronousChannelGroup");
        check(mnames.contains("openAsynchronousServerSocketChannel"), "declares method openAsynchronousServerSocketChannel");
        check(mnames.contains("openAsynchronousSocketChannel"), "declares method openAsynchronousSocketChannel");
        check(mnames.contains("toPort"), "declares method toPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
