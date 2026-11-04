/*
 * @test
 * @summary javax.management.remote.rmi._RMIConnectionImpl_Tie conformance: concrete class reflection.
 * @run main _RMIConnectionImpl_TieTest
 */

public class _RMIConnectionImpl_TieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi._RMIConnectionImpl_Tie");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("org.omg.CORBA_2_3.portable.ObjectImpl".equals(c.getSuperclass().getName()), "superclass org.omg.CORBA_2_3.portable.ObjectImpl");
        check(Class.forName("javax.rmi.CORBA.Tie").isAssignableFrom(c), "implements/extends javax.rmi.CORBA.Tie");
        check(hasMethod(c, "_ids"), "declares _ids");
        check(hasMethod(c, "_invoke"), "declares _invoke");
        check(hasMethod(c, "deactivate"), "declares deactivate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
