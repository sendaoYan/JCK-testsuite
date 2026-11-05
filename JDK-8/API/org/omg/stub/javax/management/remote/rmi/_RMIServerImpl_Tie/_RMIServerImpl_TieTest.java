/*
 * @test
 * @summary org.omg.stub.javax.management.remote.rmi._RMIServerImpl_Tie conformance: stub/tie class shape.
 * @run main _RMIServerImpl_TieTest
 */

public class _RMIServerImpl_TieTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.stub.javax.management.remote.rmi._RMIServerImpl_Tie");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA_2_3.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA_2_3.portable.ObjectImpl");
        check(javax.rmi.CORBA.Tie.class.isAssignableFrom(c), "implements javax.rmi.CORBA.Tie");
        // ---- deepened (jdk8u372-b07 probed): tie identity and hierarchy ----
        @SuppressWarnings("deprecation")
        org.omg.stub.javax.management.remote.rmi._RMIServerImpl_Tie t =
            new org.omg.stub.javax.management.remote.rmi._RMIServerImpl_Tie();
        String[] ids = t._ids();
        check(ids.length == 1, "_ids() returns exactly one id");
        check(ids[0].equals("RMI:javax.management.remote.rmi.RMIServer:0000000000000000"),
              "tie _ids()[0] equals the stub-side RMIServer id");
        check(javax.rmi.CORBA.Tie.class.isAssignableFrom(t.getClass()), "implements javax.rmi.CORBA.Tie");
        check(t.getClass().getSuperclass() == org.omg.CORBA_2_3.portable.ObjectImpl.class,
              "tie extends org.omg.CORBA_2_3.portable.ObjectImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
