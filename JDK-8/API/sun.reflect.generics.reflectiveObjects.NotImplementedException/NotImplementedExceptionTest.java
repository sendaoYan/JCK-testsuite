/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.NotImplementedException conformance: exception/error hierarchy and construction.
 * @run main NotImplementedExceptionTest
 */

public class NotImplementedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.NotImplementedException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.reflectiveObjects.NotImplementedException"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        sun.reflect.generics.reflectiveObjects.NotImplementedException x = new sun.reflect.generics.reflectiveObjects.NotImplementedException();
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
