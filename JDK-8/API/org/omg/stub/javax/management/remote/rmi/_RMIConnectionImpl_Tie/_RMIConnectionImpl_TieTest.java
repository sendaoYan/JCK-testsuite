/*
 * @test
 * @summary org.omg.stub.javax.management.remote.rmi._RMIConnectionImpl_Tie conformance: stub/tie class shape.
 * @run main _RMIConnectionImpl_TieTest
 */

public class _RMIConnectionImpl_TieTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.stub.javax.management.remote.rmi._RMIConnectionImpl_Tie");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA_2_3.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA_2_3.portable.ObjectImpl");
        check(javax.rmi.CORBA.Tie.class.isAssignableFrom(c), "implements javax.rmi.CORBA.Tie");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
