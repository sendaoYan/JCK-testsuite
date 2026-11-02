/*
 * @test
 * @summary sun.management.Util conformance: class and members.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.Util", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.Util"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkAccess"), "declares method checkAccess");
        check(mnames.contains("checkControlAccess"), "declares method checkControlAccess");
        check(mnames.contains("checkMonitorAccess"), "declares method checkMonitorAccess");
        check(mnames.contains("newException"), "declares method newException");
        check(mnames.contains("newObjectName"), "declares method newObjectName");
        check(mnames.contains("toStringArray"), "declares method toStringArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
