/*
 * @test
 * @summary sun.misc.PerformanceLogger conformance: class and members.
 * @run main PerformanceLoggerTest
 */

public class PerformanceLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.PerformanceLogger", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.PerformanceLogger"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCurrentTime"), "declares method getCurrentTime");
        check(mnames.contains("getMessageAtIndex"), "declares method getMessageAtIndex");
        check(mnames.contains("getStartTime"), "declares method getStartTime");
        check(mnames.contains("getTimeAtIndex"), "declares method getTimeAtIndex");
        check(mnames.contains("loggingEnabled"), "declares method loggingEnabled");
        check(mnames.contains("outputLog"), "declares method outputLog");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
