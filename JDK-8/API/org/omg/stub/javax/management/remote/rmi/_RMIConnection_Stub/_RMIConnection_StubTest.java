/*
 * @test
 * @summary org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub conformance: stub/tie class shape.
 * @run main _RMIConnection_StubTest
 */

public class _RMIConnection_StubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(javax.rmi.CORBA.Stub.class.isAssignableFrom(c), "extends javax.rmi.CORBA.Stub");
        check(javax.management.remote.rmi.RMIConnection.class.isAssignableFrom(c), "implements javax.management.remote.rmi.RMIConnection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
