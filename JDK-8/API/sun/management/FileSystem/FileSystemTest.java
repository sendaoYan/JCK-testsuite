/*
 * @test
 * @summary sun.management.FileSystem conformance: abstract class and members.
 * @run main FileSystemTest
 */

public class FileSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.FileSystem", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.FileSystem"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("isAccessUserOnly"), "declares method isAccessUserOnly");
        check(mnames.contains("open"), "declares method open");
        check(mnames.contains("supportsFileSecurity"), "declares method supportsFileSecurity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
