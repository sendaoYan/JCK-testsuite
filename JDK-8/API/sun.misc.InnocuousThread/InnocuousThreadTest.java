/*
 * @test
 * @summary sun.misc.InnocuousThread conformance: class and members.
 * @run main InnocuousThreadTest
 */

public class InnocuousThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.InnocuousThread", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.InnocuousThread"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Thread"), "superclass java.lang.Thread");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("eraseThreadLocals"), "declares method eraseThreadLocals");
        check(mnames.contains("getContextClassLoader"), "declares method getContextClassLoader");
        check(mnames.contains("newName"), "declares method newName");
        check(mnames.contains("newSystemThread"), "declares method newSystemThread");
        check(mnames.contains("run"), "declares method run");
        check(mnames.contains("setContextClassLoader"), "declares method setContextClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
