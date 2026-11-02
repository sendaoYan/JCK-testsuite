/*
 * @test
 * @summary sun.misc.Queue conformance: class and members.
 * @run main QueueTest
 */

public class QueueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Queue", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Queue"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("dequeue"), "declares method dequeue");
        check(mnames.contains("dump"), "declares method dump");
        check(mnames.contains("elements"), "declares method elements");
        check(mnames.contains("enqueue"), "declares method enqueue");
        check(mnames.contains("isEmpty"), "declares method isEmpty");
        check(mnames.contains("reverseElements"), "declares method reverseElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
