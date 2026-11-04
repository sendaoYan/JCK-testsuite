/*
 * @test
 * @summary java.net.SocketOptions conformance: interface hierarchy and members.
 * @run main SocketOptionsTest
 */


public class SocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SocketOptions");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOption"), "has method getOption");
        check(names.contains("setOption"), "has method setOption");        System.out.println("PASS (" + passed + " assertions)");
    }
}
