/*
 * @test
 * @summary jdk.internal.jrtfs.JrtFileSystemProvider conformance: internal package reflection.
 * @modules java.base/jdk.internal.jrtfs:+open
 * @run main JrtFileSystemProviderTest
 */
public class JrtFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jrtfs.JrtFileSystemProvider", false, JrtFileSystemProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.file.spi.FileSystemProvider"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAccess"), "method checkAccess");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "createDirectory"), "method createDirectory");
        check(hasMethod(c, "delete"), "method delete");
        check(hasMethod(c, "getFileAttributeView"), "method getFileAttributeView");
        check(hasMethod(c, "getFileStore"), "method getFileStore");
        check(hasMethod(c, "getFileSystem"), "method getFileSystem");
        check(hasMethod(c, "getPath"), "method getPath");
        check(hasMethod(c, "getScheme"), "method getScheme");
        check(hasMethod(c, "isHidden"), "method isHidden");
        check(hasMethod(c, "isSameFile"), "method isSameFile");
        check(hasMethod(c, "move"), "method move");
        check(hasMethod(c, "newAsynchronousFileChannel"), "method newAsynchronousFileChannel");
        check(hasMethod(c, "newByteChannel"), "method newByteChannel");
        check(hasMethod(c, "newDirectoryStream"), "method newDirectoryStream");
        check(hasMethod(c, "newFileChannel"), "method newFileChannel");
        check(hasMethod(c, "newFileSystem"), "method newFileSystem");
        check(hasMethod(c, "newInputStream"), "method newInputStream");
        check(hasMethod(c, "newOutputStream"), "method newOutputStream");
        check(hasMethod(c, "readAttributes"), "method readAttributes");
        check(hasMethod(c, "readSymbolicLink"), "method readSymbolicLink");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasField(c, "JRT_FS_JAR"), "field JRT_FS_JAR");
        check(hasField(c, "theFileSystem"), "field theFileSystem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
