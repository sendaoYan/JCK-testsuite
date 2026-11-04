/*
 * @test
 * @summary sun.net.www.protocol.mailto.MailToURLConnection conformance: class and members.
 * @run main MailToURLConnectionTest
 */

public class MailToURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.mailto.MailToURLConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.mailto.MailToURLConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.URLConnection"), "superclass sun.net.www.URLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("connect"), "declares method connect");
        check(mnames.contains("getConnectTimeout"), "declares method getConnectTimeout");
        check(mnames.contains("getFromAddress"), "declares method getFromAddress");
        check(mnames.contains("getOutputStream"), "declares method getOutputStream");
        check(mnames.contains("getPermission"), "declares method getPermission");
        check(mnames.contains("getReadTimeout"), "declares method getReadTimeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
