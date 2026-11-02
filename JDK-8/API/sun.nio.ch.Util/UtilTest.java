/*
 * @test
 * @summary sun.nio.ch.Util conformance: class and members.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Util", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.Util"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("_get"), "declares method _get");
        check(mnames.contains("_put"), "declares method _put");
        check(mnames.contains("atBugLevel"), "declares method atBugLevel");
        check(mnames.contains("erase"), "declares method erase");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("getMaxCachedBufferSize"), "declares method getMaxCachedBufferSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
