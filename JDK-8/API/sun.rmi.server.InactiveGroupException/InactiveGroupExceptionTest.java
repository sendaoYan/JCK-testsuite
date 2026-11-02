/*
 * @test
 * @summary sun.rmi.server.InactiveGroupException conformance: exception/error hierarchy and construction.
 * @run main InactiveGroupExceptionTest
 */

public class InactiveGroupExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.InactiveGroupException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.InactiveGroupException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.activation.ActivationException"), "superclass java.rmi.activation.ActivationException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.rmi.server.InactiveGroupException x = new sun.rmi.server.InactiveGroupException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
