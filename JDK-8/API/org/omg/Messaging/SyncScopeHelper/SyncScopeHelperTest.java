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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
