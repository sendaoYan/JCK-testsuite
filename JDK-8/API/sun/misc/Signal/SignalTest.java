/*
 * @test
 * @summary sun.misc.Signal conformance: class and members.
 * @run main SignalTest
 */

public class SignalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Signal", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Signal"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("dispatch"), "declares method dispatch");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("findSignal"), "declares method findSignal");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getNumber"), "declares method getNumber");
        check(mnames.contains("handle"), "declares method handle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
