/*
 * @test
 * @summary sun.net.httpserver.HttpsServerImpl conformance: class and members.
 * @run main HttpsServerImplTest
 */

public class HttpsServerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.httpserver.HttpsServerImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.httpserver.HttpsServerImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.HttpsServer"), "superclass com.sun.net.httpserver.HttpsServer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("createContext"), "declares method createContext");
        check(mnames.contains("getAddress"), "declares method getAddress");
        check(mnames.contains("getExecutor"), "declares method getExecutor");
        check(mnames.contains("getHttpsConfigurator"), "declares method getHttpsConfigurator");
        check(mnames.contains("removeContext"), "declares method removeContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
