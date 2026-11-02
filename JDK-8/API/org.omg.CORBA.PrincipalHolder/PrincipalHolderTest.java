/*
 * @test
 * @summary org.omg.CORBA.PrincipalHolder conformance: holder value field.
 * @run main PrincipalHolderTest
 */

public class PrincipalHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.PrincipalHolder h0 = new org.omg.CORBA.PrincipalHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Principal v = null;
        org.omg.CORBA.PrincipalHolder h1 = new org.omg.CORBA.PrincipalHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
