/*
 * @test
 * @summary javax.rmi.CORBA.Tie conformance: interface reflection.
 * @run main TieTest
 */

public class TieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Tie");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("org.omg.CORBA.portable.InvokeHandler").isAssignableFrom(c), "implements/extends org.omg.CORBA.portable.InvokeHandler");
        check(hasMethod(c, "deactivate"), "declares deactivate");
        check(hasMethod(c, "getTarget"), "declares getTarget");
        check(hasMethod(c, "orb"), "declares orb");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
