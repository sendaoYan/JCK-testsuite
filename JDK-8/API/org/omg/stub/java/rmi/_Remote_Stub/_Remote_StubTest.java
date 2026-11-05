/*
 * @test
 * @summary org.omg.stub.java.rmi._Remote_Stub conformance: stub/tie class shape.
 * @run main _Remote_StubTest
 */

public class _Remote_StubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.stub.java.rmi._Remote_Stub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(javax.rmi.CORBA.Stub.class.isAssignableFrom(c), "extends javax.rmi.CORBA.Stub");
        check(java.rmi.Remote.class.isAssignableFrom(c), "implements java.rmi.Remote");
        // ---- deepened (jdk8u372-b07 probed): stub identity table ----
        org.omg.stub.java.rmi._Remote_Stub rs = new org.omg.stub.java.rmi._Remote_Stub();
        String[] ids = rs._ids();
        check(ids.length == 1, "_ids() returns exactly one id");
        check(ids[0].equals(""), "_ids()[0] is the empty string (java.rmi.Remote has no CORBA repository id)");
        check(rs.equals(null) == false, "equals(null) is false (no delegate set, no throw)");
        check(rs.equals(rs), "equals(self) is true");
        boolean badOp = false;
        try { rs.hashCode(); } catch (org.omg.CORBA.BAD_OPERATION b) { badOp = true; }
        check(badOp, "hashCode() without delegate -> org.omg.CORBA.BAD_OPERATION");
        check(rs.toString().equals("org.omg.stub.java.rmi._Remote_Stub: no delegate set"),
              "toString() exact: <fqcn>: no delegate set");
        // serialization: javax.rmi.CORBA.Stub write_replace keeps it Serializable
        check(java.io.Serializable.class.isAssignableFrom(c), "stub is Serializable");
        org.omg.stub.java.rmi._Remote_Stub rs2 = new org.omg.stub.java.rmi._Remote_Stub();
        check(rs2._ids().length == 1 && rs2._ids()[0].equals(""), "fresh stub repeats the empty id table");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
