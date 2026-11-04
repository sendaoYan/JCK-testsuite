/*
 * @test
 * @summary sun.rmi.server.UnicastServerRef2 conformance: class and members.
 * @run main UnicastServerRef2Test
 */

public class UnicastServerRef2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.UnicastServerRef2", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.UnicastServerRef2"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.server.UnicastServerRef"), "superclass sun.rmi.server.UnicastServerRef");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getClientRef"), "declares method getClientRef");
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
