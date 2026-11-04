/*
 * @test
 * @summary jdk.internal.loader.NativeLibraries conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main NativeLibrariesTest
 */
public class NativeLibrariesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.NativeLibraries", false, NativeLibrariesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "find"), "method find");
        check(hasMethod(c, "loadLibrary"), "method loadLibrary");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "caller"), "field caller");
        check(hasField(c, "libraries"), "field libraries");
        check(hasField(c, "loadLibraryOnlyIfPresent"), "field loadLibraryOnlyIfPresent");
        check(hasField(c, "loader"), "field loader");
        check(hasField(c, "nativeLibraryLockMap"), "field nativeLibraryLockMap");
        check(hasField(c, "searchJavaLibraryPath"), "field searchJavaLibraryPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
