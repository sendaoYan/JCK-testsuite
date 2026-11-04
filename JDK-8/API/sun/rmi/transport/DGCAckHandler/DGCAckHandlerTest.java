/*
 * @test
 * @summary sun.rmi.transport.DGCAckHandler conformance: class and members.
 * @run main DGCAckHandlerTest
 */

public class DGCAckHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.DGCAckHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.DGCAckHandler"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("received"), "declares method received");
        check(mnames.contains("release"), "declares method release");
        check(mnames.contains("startTimer"), "declares method startTimer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
