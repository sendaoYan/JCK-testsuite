/*
 * @test
 * @summary java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject conformance: inner condition.
 * @run main ConditionObjectTest
 */


public class ConditionObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.concurrent.locks.AbstractQueuedSynchronizer aqs = new java.util.concurrent.locks.AbstractQueuedSynchronizer() {};
        java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject c = aqs.new ConditionObject();
        check(c instanceof java.util.concurrent.locks.Condition, "is Condition");
        try {
            c.signal();
            throw new AssertionError("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            check(true, "signal without lock throws");
        }        System.out.println("PASS (" + passed + " assertions)");
    }
}
