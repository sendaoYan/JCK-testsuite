/*
 * @test
 * @summary org.omg.Messaging.SyncScopeHelper conformance: helper static id/type/narrow.
 * @run main SyncScopeHelperTest
 */

public class SyncScopeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.Messaging.SyncScopeHelper.id() != null, "id() non-null");
        check(org.omg.Messaging.SyncScopeHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper full contract ----
        check(org.omg.Messaging.SyncScopeHelper.id().equals("IDL:omg.org/Messaging/SyncScope:1.0"),
              "id() exact repository id");
        check(org.omg.Messaging.SyncScopeHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias (typedef of unsigned short)");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.Messaging.SyncScopeHelper.write(os, org.omg.Messaging.SYNC_WITH_TRANSPORT.value);
        check(org.omg.Messaging.SyncScopeHelper.read(os.create_input_stream()) == (short) 1,
              "wire round-trip value 1");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.Messaging.SyncScopeHelper.insert(a, (short) 3);
        check(org.omg.Messaging.SyncScopeHelper.extract(a) == (short) 3, "Any insert/extract round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any type after insert is tk_alias");
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.Messaging.SyncScopeHelper.write(os2, Short.MAX_VALUE);
        check(org.omg.Messaging.SyncScopeHelper.read(os2.create_input_stream()) == Short.MAX_VALUE,
              "wire round-trip Short.MAX_VALUE");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
