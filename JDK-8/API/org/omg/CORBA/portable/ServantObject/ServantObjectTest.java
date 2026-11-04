/*
 * @test
 * @summary org.omg.CORBA.portable.ServantObject conformance: concrete class behavior.
 * @run main ServantObjectTest
 */

public class ServantObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.portable.ServantObject so = new org.omg.CORBA.portable.ServantObject();
        check(so.servant == null, "servant default null");
        so.servant = "servant";
        check("servant".equals(so.servant), "servant field assignable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
