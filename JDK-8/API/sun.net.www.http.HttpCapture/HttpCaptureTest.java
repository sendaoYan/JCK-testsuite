/*
 * @test
 * @summary sun.net.www.http.HttpCapture conformance: class and members.
 * @run main HttpCaptureTest
 */

public class HttpCaptureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.HttpCapture", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.HttpCapture"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("flush"), "declares method flush");
        check(mnames.contains("getCapture"), "declares method getCapture");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("isInitialized"), "declares method isInitialized");
        check(mnames.contains("received"), "declares method received");
        check(mnames.contains("sent"), "declares method sent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
