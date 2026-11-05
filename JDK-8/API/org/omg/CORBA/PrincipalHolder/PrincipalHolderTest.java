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
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.PrincipalHolder().value == null, "default value is null");
        check(new org.omg.CORBA.PrincipalHolder(null).value == null, "ctor stores null");
        check(new org.omg.CORBA.PrincipalHolder()._type().kind() == org.omg.CORBA.TCKind.tk_Principal,
              "PrincipalHolder._type().kind() == tk_Principal (deprecated IDL type)");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.PrincipalHolder.class),
              "PrincipalHolder implements org.omg.CORBA.portable.Streamable");
        try {
            org.omg.CORBA.portable.OutputStream osPr = orb.create_output_stream(); new org.omg.CORBA.PrincipalHolder(null)._write(osPr);
            throw new AssertionError("PrincipalHolder(null)._write (deprecated, jdk8u372 cannot marshal null Principal) should throw java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            check(true, "PrincipalHolder(null)._write (deprecated, jdk8u372 cannot marshal null Principal) -> java.lang.NullPointerException");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
