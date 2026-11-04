/*
 * @test
 * @summary sun.net.httpserver.HttpServerImpl conformance: class and members.
 * @run main HttpServerImplTest
 */

public class HttpServerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.httpserver.HttpServerImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.httpserver.HttpServerImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.HttpServer"), "superclass com.sun.net.httpserver.HttpServer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("createContext"), "declares method createContext");
        check(mnames.contains("getAddress"), "declares method getAddress");
        check(mnames.contains("getExecutor"), "declares method getExecutor");
        check(mnames.contains("removeContext"), "declares method removeContext");
        check(mnames.contains("setExecutor"), "declares method setExecutor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
