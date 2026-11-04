/*
 * @test
 * @summary sun.rmi.log.ReliableLog.LogFile conformance: class and members.
 * @run main LogFileTest
 */

public class LogFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.log.ReliableLog$LogFile", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.log.ReliableLog$LogFile"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.RandomAccessFile"), "superclass java.io.RandomAccessFile");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkSpansBoundary"), "declares method checkSpansBoundary");
        check(mnames.contains("sync"), "declares method sync");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
