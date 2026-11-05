/*
 * @test
 * @summary org.omg.CORBA.UnknownUserExceptionHolder conformance: holder value field.
 * @run main UnknownUserExceptionHolderTest
 */

public class UnknownUserExceptionHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UnknownUserExceptionHolder h0 = new org.omg.CORBA.UnknownUserExceptionHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.UnknownUserException v = null;
        org.omg.CORBA.UnknownUserExceptionHolder h1 = new org.omg.CORBA.UnknownUserExceptionHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.UnknownUserExceptionHolder().value == null, "default value is null");
        org.omg.CORBA.Any anyUu = orb.create_any();
        anyUu.insert_string("boom");
        org.omg.CORBA.UnknownUserException uue = new org.omg.CORBA.UnknownUserException(anyUu);
        check(new org.omg.CORBA.UnknownUserExceptionHolder(uue).value == uue, "ctor stores same reference");
        check(new org.omg.CORBA.UnknownUserExceptionHolder()._type().kind() == org.omg.CORBA.TCKind.tk_except,
              "UnknownUserExceptionHolder._type().kind() == tk_except");
        org.omg.CORBA.portable.OutputStream osUu = orb.create_output_stream();
        new org.omg.CORBA.UnknownUserExceptionHolder(uue)._write(osUu);
        org.omg.CORBA.UnknownUserExceptionHolder whUu = new org.omg.CORBA.UnknownUserExceptionHolder();
        whUu._read(osUu.create_input_stream());
        check("boom".equals(whUu.value.except.extract_string()), "CDR wire round-trip keeps nested any");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
