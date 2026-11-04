/*
 * @test
 * @summary sun.nio.fs.AbstractFileSystemProvider conformance: internal package reflection.
 * @modules java.base/sun.nio.fs:+open
 * @run main AbstractFileSystemProviderTest
 */
public class AbstractFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.AbstractFileSystemProvider", false, AbstractFileSystemProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.file.spi.FileSystemProvider"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "delete"), "method delete");
        check(hasMethod(c, "deleteIfExists"), "method deleteIfExists");
        check(hasMethod(c, "getSunPathForSocketFile"), "method getSunPathForSocketFile");
        check(hasMethod(c, "isExecutable"), "method isExecutable");
        check(hasMethod(c, "isReadable"), "method isReadable");
        check(hasMethod(c, "isWritable"), "method isWritable");
        check(hasMethod(c, "readAttributes"), "method readAttributes");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
