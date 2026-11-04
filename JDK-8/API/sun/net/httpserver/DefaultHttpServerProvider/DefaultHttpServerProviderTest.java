/*
 * @test
 * @summary sun.net.httpserver.DefaultHttpServerProvider conformance: class and members.
 * @run main DefaultHttpServerProviderTest
 */

public class DefaultHttpServerProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.httpserver.DefaultHttpServerProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.httpserver.DefaultHttpServerProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.spi.HttpServerProvider"), "superclass com.sun.net.httpserver.spi.HttpServerProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createHttpServer"), "declares method createHttpServer");
        check(mnames.contains("createHttpsServer"), "declares method createHttpsServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
