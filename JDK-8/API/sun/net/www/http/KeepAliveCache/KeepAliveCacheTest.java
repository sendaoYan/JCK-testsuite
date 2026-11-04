/*
 * @test
 * @summary sun.net.www.http.KeepAliveCache conformance: class and members.
 * @run main KeepAliveCacheTest
 */

public class KeepAliveCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.KeepAliveCache", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.KeepAliveCache"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.HashMap"), "superclass java.util.HashMap");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Runnable"), "interface java.lang.Runnable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getMaxConnections"), "declares method getMaxConnections");
        check(mnames.contains("put"), "declares method put");
        check(mnames.contains("readObject"), "declares method readObject");
        check(mnames.contains("remove"), "declares method remove");
        check(mnames.contains("removeVector"), "declares method removeVector");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
