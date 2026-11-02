/*
 * @test
 * @summary org.omg.CORBA.Principal conformance: concrete class behavior.
 * @run main PrincipalTest
 */

public class PrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.Principal p = new org.omg.CORBA.Principal();
        check(p != null, "constructs");
        try {
            p.name();
            throw new AssertionError("name() should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            passed++;
        }
        try {
            p.name(new byte[]{1});
            throw new AssertionError("name(byte[]) should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            passed++;
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
