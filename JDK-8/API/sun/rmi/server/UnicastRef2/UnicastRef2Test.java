/*
 * @test
 * @summary sun.rmi.server.UnicastRef2 conformance: class and members.
 * @run main UnicastRef2Test
 */

public class UnicastRef2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.UnicastRef2", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.UnicastRef2"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.server.UnicastRef"), "superclass sun.rmi.server.UnicastRef");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        check(mnames.contains("readExternal"), "declares method readExternal");
        check(mnames.contains("writeExternal"), "declares method writeExternal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
