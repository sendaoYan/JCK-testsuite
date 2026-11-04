/*
 * @test
 * @summary sun.rmi.log.ReliableLog conformance: class and members.
 * @run main ReliableLogTest
 */

public class ReliableLogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.log.ReliableLog", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.log.ReliableLog"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("commitToNewVersion"), "declares method commitToNewVersion");
        check(mnames.contains("createFirstVersion"), "declares method createFirstVersion");
        check(mnames.contains("deleteFile"), "declares method deleteFile");
        check(mnames.contains("deleteLogFile"), "declares method deleteLogFile");
        check(mnames.contains("deleteNewVersionFile"), "declares method deleteNewVersionFile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
