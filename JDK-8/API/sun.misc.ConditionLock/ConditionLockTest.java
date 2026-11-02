/*
 * @test
 * @summary sun.misc.ConditionLock conformance: class and members.
 * @run main ConditionLockTest
 */

public class ConditionLockTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ConditionLock", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ConditionLock"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.misc.Lock"), "superclass sun.misc.Lock");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("lockWhen"), "declares method lockWhen");
        check(mnames.contains("unlockWith"), "declares method unlockWith");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
