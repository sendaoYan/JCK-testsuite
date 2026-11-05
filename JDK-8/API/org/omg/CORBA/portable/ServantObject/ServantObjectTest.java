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

        // ---- deepened: shape ----
        check(!java.lang.reflect.Modifier.isAbstract(org.omg.CORBA.portable.ServantObject.class.getModifiers()),
              "concrete class");
        org.omg.CORBA.portable.ServantObject so2 = new org.omg.CORBA.portable.ServantObject();
        check(so2.servant == null, "second instance also defaults to null");
        java.lang.Object any = new java.lang.Object();
        so2.servant = any;
        check(so2.servant == any, "servant accepts arbitrary Object");
        check(org.omg.CORBA.portable.ServantObject.class.getFields().length == 1
              && "servant".equals(org.omg.CORBA.portable.ServantObject.class.getFields()[0].getName()),
              "single public field 'servant'");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
