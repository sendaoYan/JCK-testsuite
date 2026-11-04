/*
 * @test
 * @summary sun.misc.ExtensionInstallationException conformance: exception/error hierarchy and construction.
 * @run main ExtensionInstallationExceptionTest
 */

public class ExtensionInstallationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ExtensionInstallationException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ExtensionInstallationException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.misc.ExtensionInstallationException x = new sun.misc.ExtensionInstallationException("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
