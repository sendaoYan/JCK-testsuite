/*
 * @test
 * @summary sun.net.ProgressEvent conformance: class and members.
 * @run main ProgressEventTest
 */

public class ProgressEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ProgressEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ProgressEvent"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getContentType"), "declares method getContentType");
        check(mnames.contains("getExpected"), "declares method getExpected");
        check(mnames.contains("getMethod"), "declares method getMethod");
        check(mnames.contains("getProgress"), "declares method getProgress");
        check(mnames.contains("getState"), "declares method getState");
        check(mnames.contains("getURL"), "declares method getURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
