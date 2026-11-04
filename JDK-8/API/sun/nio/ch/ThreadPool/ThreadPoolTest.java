/*
 * @test
 * @summary sun.nio.ch.ThreadPool conformance: class and members.
 * @run main ThreadPoolTest
 */

public class ThreadPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.ThreadPool", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.ThreadPool"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("createDefault"), "declares method createDefault");
        check(mnames.contains("defaultThreadFactory"), "declares method defaultThreadFactory");
        check(mnames.contains("executor"), "declares method executor");
        check(mnames.contains("getDefault"), "declares method getDefault");
        check(mnames.contains("getDefaultThreadPoolInitialSize"), "declares method getDefaultThreadPoolInitialSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
