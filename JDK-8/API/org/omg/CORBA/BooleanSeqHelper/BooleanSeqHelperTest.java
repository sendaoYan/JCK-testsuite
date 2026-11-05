/*
 * @test
 * @summary org.omg.CORBA.BooleanSeqHelper conformance: helper static id/type/narrow.
 * @run main BooleanSeqHelperTest
 */

public class BooleanSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.BooleanSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.BooleanSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.BooleanSeqHelper.id().equals("IDL:omg.org/CORBA/BooleanSeq:1.0"),
              "BooleanSeqHelper.id() exact repository id");
        check(org.omg.CORBA.BooleanSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "BooleanSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.BooleanSeqHelper.write(osH, new boolean[]{true});
        check(java.util.Arrays.equals(org.omg.CORBA.BooleanSeqHelper.read(osH.create_input_stream()), new boolean[]{true}),
              "BooleanSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.BooleanSeqHelper.insert(anyH, new boolean[]{true});
        check(java.util.Arrays.equals(org.omg.CORBA.BooleanSeqHelper.extract(anyH), new boolean[]{true}),
              "BooleanSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
