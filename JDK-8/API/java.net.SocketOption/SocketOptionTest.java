/*
 * @test
 * @summary java.net.SocketOption conformance: interface hierarchy and members.
 * @run main SocketOptionTest
 */


public class SocketOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketOption");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("type"), "has method type");        System.out.println("PASS (" + passed + " assertions)");
    }
}
