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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
