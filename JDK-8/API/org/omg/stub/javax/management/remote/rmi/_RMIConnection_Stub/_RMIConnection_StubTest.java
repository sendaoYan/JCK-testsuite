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
        // ---- deepened (jdk8u372-b07 probed): JMX stub identity table ----
        @SuppressWarnings("deprecation")
        org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub s =
            new org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub();
        String[] ids = s._ids();
        check(ids.length == 1, "_ids() returns exactly one id");
        check(ids[0].equals("RMI:javax.management.remote.rmi.RMIConnection:0000000000000000"),
              "_ids()[0] exact RMI-format repository id with zero hash");
        check(s.toString().startsWith("org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub"),
              "toString prefix is the class name");
        check(javax.management.remote.rmi.RMIConnection.class.isAssignableFrom(s.getClass()),
              "stub implements RMIConnection business interface");
        check(!java.lang.reflect.Modifier.isFinal(s.getClass().getModifiers()), "stub class is not final");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
