/*
 * @test
 * @summary sun.tools.jar.Main conformance: class and members.
 * @run main MainTest
 */

public class MainTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.jar.Main", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.tools.jar.Main"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addCreatedBy"), "declares method addCreatedBy");
        check(mnames.contains("addFile"), "declares method addFile");
        check(mnames.contains("addIndex"), "declares method addIndex");
        check(mnames.contains("addMainClass"), "declares method addMainClass");
        check(mnames.contains("addVersion"), "declares method addVersion");
        check(mnames.contains("copy"), "declares method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
