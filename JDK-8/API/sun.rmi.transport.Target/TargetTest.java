/*
 * @test
 * @summary sun.rmi.transport.Target conformance: class and members.
 * @run main TargetTest
 */

public class TargetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.Target", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.Target"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkLoaderAncestry"), "declares method checkLoaderAncestry");
        check(mnames.contains("decrementCallCount"), "declares method decrementCallCount");
        check(mnames.contains("getAccessControlContext"), "declares method getAccessControlContext");
        check(mnames.contains("getContextClassLoader"), "declares method getContextClassLoader");
        check(mnames.contains("getDispatcher"), "declares method getDispatcher");
        check(mnames.contains("getImpl"), "declares method getImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
