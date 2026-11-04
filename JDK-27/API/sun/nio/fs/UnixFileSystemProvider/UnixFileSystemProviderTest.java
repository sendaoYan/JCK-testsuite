/*
 * @test
 * @summary sun.nio.fs.UnixFileSystemProvider conformance: internal package reflection.
 * @modules java.base/sun.nio.fs:+open
 * @run main UnixFileSystemProviderTest
 */
public class UnixFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.UnixFileSystemProvider", false, UnixFileSystemProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.fs.AbstractFileSystemProvider"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAccess"), "method checkAccess");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "createDirectory"), "method createDirectory");
        check(hasMethod(c, "createLink"), "method createLink");
        check(hasMethod(c, "createSymbolicLink"), "method createSymbolicLink");
        check(hasMethod(c, "exists"), "method exists");
        check(hasMethod(c, "getFileAttributeView"), "method getFileAttributeView");
        check(hasMethod(c, "getFileStore"), "method getFileStore");
        check(hasMethod(c, "getFileSystem"), "method getFileSystem");
        check(hasMethod(c, "getPath"), "method getPath");
        check(hasMethod(c, "getScheme"), "method getScheme");
        check(hasMethod(c, "getSunPathForSocketFile"), "method getSunPathForSocketFile");
        check(hasMethod(c, "isExecutable"), "method isExecutable");
        check(hasMethod(c, "isHidden"), "method isHidden");
        check(hasMethod(c, "isReadable"), "method isReadable");
        check(hasMethod(c, "isSameFile"), "method isSameFile");
        check(hasMethod(c, "isWritable"), "method isWritable");
        check(hasMethod(c, "move"), "method move");
        check(hasMethod(c, "newAsynchronousFileChannel"), "method newAsynchronousFileChannel");
        check(hasMethod(c, "newByteChannel"), "method newByteChannel");
        check(hasMethod(c, "newDirectoryStream"), "method newDirectoryStream");
        check(hasMethod(c, "newFileChannel"), "method newFileChannel");
        check(hasMethod(c, "newFileSystem"), "method newFileSystem");
        check(hasMethod(c, "readAttributes"), "method readAttributes");
        check(hasMethod(c, "readAttributesIfExists"), "method readAttributesIfExists");
        check(hasMethod(c, "readSymbolicLink"), "method readSymbolicLink");
        check(hasField(c, "EMPTY_PATH"), "field EMPTY_PATH");
        check(hasField(c, "theFileSystem"), "field theFileSystem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
