/*
 * @test
 * @summary sun.misc.Timer conformance: class and members.
 * @run main TimerTest
 */

public class TimerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Timer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Timer"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("cont"), "declares method cont");
        check(mnames.contains("getInterval"), "declares method getInterval");
        check(mnames.contains("getRemainingTime"), "declares method getRemainingTime");
        check(mnames.contains("getStopTime"), "declares method getStopTime");
        check(mnames.contains("getTimerThread"), "declares method getTimerThread");
        check(mnames.contains("isStopped"), "declares method isStopped");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
