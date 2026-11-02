/*
 * @test
 * @summary sun.nio.cs.ext.MS50221 conformance: class and members.
 * @run main MS50221Test
 */

public class MS50221Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.MS50221", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.MS50221"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.MS50220"), "superclass sun.nio.cs.ext.MS50220");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("doSBKANA"), "declares method doSBKANA");
        check(mnames.contains("historicalName"), "declares method historicalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
