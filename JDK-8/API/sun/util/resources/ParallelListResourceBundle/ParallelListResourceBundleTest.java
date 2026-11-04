/*
 * @test
 * @summary sun.util.resources.ParallelListResourceBundle conformance: abstract class and members.
 * @run main ParallelListResourceBundleTest
 */

public class ParallelListResourceBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.ParallelListResourceBundle", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.resources.ParallelListResourceBundle"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ResourceBundle"), "superclass java.util.ResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("areParallelContentsComplete"), "declares method areParallelContentsComplete");
        check(mnames.contains("containsKey"), "declares method containsKey");
        check(mnames.contains("getContents"), "declares method getContents");
        check(mnames.contains("getKeys"), "declares method getKeys");
        check(mnames.contains("getParent"), "declares method getParent");
        check(mnames.contains("handleGetObject"), "declares method handleGetObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
