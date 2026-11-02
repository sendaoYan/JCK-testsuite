/*
 * @test
 * @summary sun.net.www.protocol.http.AuthCacheImpl conformance: class and members.
 * @run main AuthCacheImplTest
 */

public class AuthCacheImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.AuthCacheImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.net.www.protocol.http.AuthCache"), "interface sun.net.www.protocol.http.AuthCache");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("put"), "declares method put");
        check(mnames.contains("remove"), "declares method remove");
        check(mnames.contains("setMap"), "declares method setMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
