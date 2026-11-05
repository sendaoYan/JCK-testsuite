/*
 * @test
 * @summary org.omg.CORBA.WrongTransactionHelper conformance: helper static id/type/narrow.
 * @run main WrongTransactionHelperTest
 */

public class WrongTransactionHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WrongTransactionHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WrongTransactionHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.WrongTransactionHelper.id().equals("IDL:omg.org/CORBA/WrongTransaction:1.0"),
              "WrongTransactionHelper.id() exact repository id");
        check(org.omg.CORBA.WrongTransactionHelper.id().startsWith("IDL:"), "repository id starts with IDL:");
        check(org.omg.CORBA.WrongTransactionHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "WrongTransactionHelper.type().kind() == tk_except");
        org.omg.CORBA.portable.OutputStream osWt = orb.create_output_stream();
        org.omg.CORBA.WrongTransactionHelper.write(osWt, new org.omg.CORBA.WrongTransaction("w"));
        check(org.omg.CORBA.WrongTransactionHelper.id().equals(
                org.omg.CORBA.WrongTransactionHelper.read(osWt.create_input_stream()).getMessage()),
              "write/read CDR round-trip message is the repository id");
        org.omg.CORBA.Any anyWt = orb.create_any();
        org.omg.CORBA.WrongTransactionHelper.insert(anyWt, new org.omg.CORBA.WrongTransaction("why"));
        check(org.omg.CORBA.WrongTransactionHelper.id().equals(
                org.omg.CORBA.WrongTransactionHelper.extract(anyWt).getMessage()),
              "insert/extract Any round-trip: ctor message is not marshalled, extract returns repo-id message");
        check(org.omg.CORBA.WrongTransactionHelper.id().equals(new org.omg.CORBA.WrongTransaction().getMessage()),
              "WrongTransaction() no-arg ctor message is the repository id (jdk8u372)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
