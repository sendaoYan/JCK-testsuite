/*
 * @test
 * @summary javax.rmi.CORBA.ValueHandler conformance: interface reflection.
 * @run main ValueHandlerTest
 */

public class ValueHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.ValueHandler");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getRMIRepositoryID"), "declares getRMIRepositoryID");
        check(hasMethod(c, "getRunTimeCodeBase"), "declares getRunTimeCodeBase");
        check(hasMethod(c, "isCustomMarshaled"), "declares isCustomMarshaled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
