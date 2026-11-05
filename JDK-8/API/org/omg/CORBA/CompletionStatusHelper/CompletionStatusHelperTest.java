/*
 * @test
 * @summary org.omg.CORBA.CompletionStatusHelper conformance: helper static id/type/narrow.
 * @run main CompletionStatusHelperTest
 */

public class CompletionStatusHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CompletionStatusHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CompletionStatusHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.CompletionStatusHelper.id().equals("IDL:omg.org/CORBA/CompletionStatus:1.0"),
              "CompletionStatusHelper.id() exact repository id");
        check(org.omg.CORBA.CompletionStatusHelper.type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "CompletionStatusHelper.type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.CORBA.CompletionStatusHelper.write(osE, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        check(org.omg.CORBA.CompletionStatusHelper.read(osE.create_input_stream()) == org.omg.CORBA.CompletionStatus.COMPLETED_YES,
              "CompletionStatusHelper.write/read round-trips to the canonical constant (identity)");
        org.omg.CORBA.Any anyE = orb.create_any();
        org.omg.CORBA.CompletionStatusHelper.insert(anyE, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        check(org.omg.CORBA.CompletionStatusHelper.extract(anyE) == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
              "CompletionStatusHelper.insert/extract round-trips to the canonical constant (identity)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
