/*
 * @test
 * @summary org.omg.CORBA.UnknownUserExceptionHelper conformance: helper static id/type/narrow.
 * @run main UnknownUserExceptionHelperTest
 */

public class UnknownUserExceptionHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UnknownUserExceptionHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.UnknownUserExceptionHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.UnknownUserExceptionHelper.id().equals("IDL:omg.org/CORBA/UnknownUserException:1.0"),
              "UnknownUserExceptionHelper.id() exact repository id");
        check(org.omg.CORBA.UnknownUserExceptionHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "UnknownUserExceptionHelper.type().kind() == tk_except");
        org.omg.CORBA.Any anyUu = orb.create_any();
        anyUu.insert_string("boom");
        org.omg.CORBA.UnknownUserException uue = new org.omg.CORBA.UnknownUserException(anyUu);
        org.omg.CORBA.portable.OutputStream osUu = orb.create_output_stream();
        org.omg.CORBA.UnknownUserExceptionHelper.write(osUu, uue);
        check("boom".equals(org.omg.CORBA.UnknownUserExceptionHelper.read(osUu.create_input_stream()).except.extract_string()),
              "write/read CDR round-trip keeps nested any");
        org.omg.CORBA.Any anyHu = orb.create_any();
        org.omg.CORBA.UnknownUserExceptionHelper.insert(anyHu, uue);
        check("boom".equals(org.omg.CORBA.UnknownUserExceptionHelper.extract(anyHu).except.extract_string()),
              "insert/extract Any round-trip keeps nested any");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
