/*
 * @test
 * @summary jdk.nio.zipfs.ZipFileSystemProvider conformance: internal class reflection.
 * @modules jdk.zipfs/jdk.nio.zipfs:+open
 * @run main ZipFileSystemProviderTest
 */
import java.lang.reflect.Constructor;

public class ZipFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.nio.zipfs.ZipFileSystemProvider");
        Object zfp = c.getDeclaredConstructor().newInstance();
        check(c.getMethod("getScheme").invoke(zfp).equals("jar"), "scheme jar");
        check(c.getMethod("getScheme").invoke(zfp).equals(c.getMethod("getScheme").invoke(zfp)), "scheme stable");
        java.io.File zip = java.io.File.createTempFile("zipfsdeepen", ".zip");
        zip.deleteOnExit();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zip))) {
            zos.putNextEntry(new java.util.zip.ZipEntry("dir/hello.txt"));
            zos.write("hi zip".getBytes("UTF-8"));
            zos.closeEntry();
        }
        java.nio.file.FileSystem zfs = (java.nio.file.FileSystem)
                c.getMethod("newFileSystem", java.nio.file.Path.class, java.util.Map.class)
                        .invoke(zfp, zip.toPath(), java.util.Map.of());
        check(zfs.isOpen(), "fs open");
        check(zfs.getSeparator().equals("/"), "separator slash");
        java.nio.file.Path hello = zfs.getPath("/dir/hello.txt");
        check(java.nio.file.Files.exists(hello), "entry exists");
        check(java.nio.file.Files.size(hello) == 6, "entry size");
        check(new String(java.nio.file.Files.readAllBytes(hello), "UTF-8").equals("hi zip"), "entry content round-trip");
        try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(zfs.getPath("/added.txt"))) {
            os.write("added".getBytes("UTF-8"));
        }
        check(java.nio.file.Files.size(zfs.getPath("/added.txt")) == 5, "new entry written");
        zfs.close();
        check(!zfs.isOpen(), "closed");
        java.nio.file.FileSystem reopen = (java.nio.file.FileSystem)
                c.getMethod("newFileSystem", java.nio.file.Path.class, java.util.Map.class)
                        .invoke(zfp, zip.toPath(), java.util.Map.of());
        check(new String(java.nio.file.Files.readAllBytes(reopen.getPath("/added.txt")), "UTF-8").equals("added"),
                "write persisted to real zip");
        check(java.nio.file.Files.exists(reopen.getPath("/dir/hello.txt")), "original entry preserved");
        reopen.close();
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.nio.zipfs.ZipFileSystemProvider", false, ZipFileSystemProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.nio.zipfs.ZipFileSystemProvider"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.file.spi.FileSystemProvider"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("checkAccess;copy;createDirectory;delete;exists;getFileAttributeView;getFileStore;getFileSystem;getPath;getScheme;isHidden;isSameFile;move;newAsynchronousFileChannel;newByteChannel;newDirectoryStream;newFileChannel;newFileSystem;newInputStream;newOutputStream;readAttributes;readAttributesIfExists;readSymbolicLink;setAttribute;uriToPath"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
