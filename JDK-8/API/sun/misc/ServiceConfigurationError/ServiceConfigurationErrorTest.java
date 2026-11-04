/*
 * @test
 * @summary sun.misc.ServiceConfigurationError conformance: exception/error hierarchy and construction.
 * @run main ServiceConfigurationErrorTest
 */

public class ServiceConfigurationErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ServiceConfigurationError", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ServiceConfigurationError"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.misc.ServiceConfigurationError x = new sun.misc.ServiceConfigurationError("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
