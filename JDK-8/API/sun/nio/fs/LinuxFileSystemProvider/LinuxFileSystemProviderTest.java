/*
 * @test
 * @summary sun.nio.fs.LinuxFileSystemProvider conformance: class and members.
 * @run main LinuxFileSystemProviderTest
 */

public class LinuxFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.LinuxFileSystemProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.fs.LinuxFileSystemProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.fs.UnixFileSystemProvider"), "superclass sun.nio.fs.UnixFileSystemProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFileAttributeView"), "declares method getFileAttributeView");
        check(mnames.contains("getFileStore"), "declares method getFileStore");
        check(mnames.contains("getFileTypeDetector"), "declares method getFileTypeDetector");
        check(mnames.contains("newFileSystem"), "declares method newFileSystem");
        check(mnames.contains("readAttributes"), "declares method readAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
