/*
 * @test
 * @summary sun.net.ProgressSource conformance: class and members.
 * @run main ProgressSourceTest
 */

public class ProgressSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ProgressSource", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ProgressSource"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("beginTracking"), "declares method beginTracking");
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("connected"), "declares method connected");
        check(mnames.contains("finishTracking"), "declares method finishTracking");
        check(mnames.contains("getContentType"), "declares method getContentType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
