/*
 * @test
 * @summary sun.rmi.server.MarshalOutputStream conformance: class and members.
 * @run main MarshalOutputStreamTest
 */

public class MarshalOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.MarshalOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.MarshalOutputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ObjectOutputStream"), "superclass java.io.ObjectOutputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("annotateClass"), "declares method annotateClass");
        check(mnames.contains("annotateProxyClass"), "declares method annotateProxyClass");
        check(mnames.contains("replaceObject"), "declares method replaceObject");
        check(mnames.contains("writeLocation"), "declares method writeLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
