/*
 * @test
 * @summary sun.nio.ch.FileChannelImpl conformance: class and members.
 * @run main FileChannelImplTest
 */

public class FileChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.FileChannelImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.FileChannelImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.FileChannel"), "superclass java.nio.channels.FileChannel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("ensureOpen"), "declares method ensureOpen");
        check(mnames.contains("fileLockTable"), "declares method fileLockTable");
        check(mnames.contains("force"), "declares method force");
        check(mnames.contains("getMappedBufferPool"), "declares method getMappedBufferPool");
        check(mnames.contains("implCloseChannel"), "declares method implCloseChannel");
        check(mnames.contains("initIDs"), "declares method initIDs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
