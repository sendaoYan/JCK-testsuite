/*
 * @test
 * @summary java.net.SocketAddress conformance: abstract class hierarchy and members.
 * @run main SocketAddressTest
 */


public class SocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketAddress");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");        System.out.println("PASS (" + passed + " assertions)");
    }
}
