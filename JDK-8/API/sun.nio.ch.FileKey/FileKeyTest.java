/*
 * @test
 * @summary sun.nio.ch.FileKey conformance: class and members.
 * @run main FileKeyTest
 */

public class FileKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.FileKey", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.FileKey"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("hashCode"), "declares method hashCode");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("initIDs"), "declares method initIDs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
