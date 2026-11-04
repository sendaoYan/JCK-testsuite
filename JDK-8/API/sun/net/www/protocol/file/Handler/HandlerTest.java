/*
 * @test
 * @summary sun.net.www.protocol.file.Handler conformance: class and members.
 * @run main HandlerTest
 */

public class HandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.file.Handler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.file.Handler"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.URLStreamHandler"), "superclass java.net.URLStreamHandler");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createFileURLConnection"), "declares method createFileURLConnection");
        check(mnames.contains("getHost"), "declares method getHost");
        check(mnames.contains("hostsEqual"), "declares method hostsEqual");
        check(mnames.contains("openConnection"), "declares method openConnection");
        check(mnames.contains("parseURL"), "declares method parseURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
