/*
 * @test
 * @summary java.net.DatagramSocketImplFactory conformance: interface hierarchy and members.
 * @run main DatagramSocketImplFactoryTest
 */


public class DatagramSocketImplFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImplFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createDatagramSocketImpl"), "has method createDatagramSocketImpl");        System.out.println("PASS (" + passed + " assertions)");
    }
}
