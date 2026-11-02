/*
 * @test
 * @summary sun.rmi.transport.ObjectTable conformance: class and members.
 * @run main ObjectTableTest
 */

public class ObjectTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.ObjectTable", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.ObjectTable"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decrementKeepAliveCount"), "declares method decrementKeepAliveCount");
        check(mnames.contains("getStub"), "declares method getStub");
        check(mnames.contains("getTarget"), "declares method getTarget");
        check(mnames.contains("incrementKeepAliveCount"), "declares method incrementKeepAliveCount");
        check(mnames.contains("putTarget"), "declares method putTarget");
        check(mnames.contains("referenced"), "declares method referenced");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
