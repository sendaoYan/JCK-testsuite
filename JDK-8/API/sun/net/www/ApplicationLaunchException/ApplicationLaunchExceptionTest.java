/*
 * @test
 * @summary sun.net.www.ApplicationLaunchException conformance: exception/error hierarchy and construction.
 * @run main ApplicationLaunchExceptionTest
 */

public class ApplicationLaunchExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.ApplicationLaunchException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.ApplicationLaunchException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.net.www.ApplicationLaunchException x = new sun.net.www.ApplicationLaunchException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
