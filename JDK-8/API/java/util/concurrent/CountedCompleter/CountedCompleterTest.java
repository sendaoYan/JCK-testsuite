/*
 * @test
 * @summary java.util.concurrent.CountedCompleter API conformance: completer/pending-count.
 * @run main CountedCompleterTest
 */

import java.util.concurrent.CountedCompleter;

public class CountedCompleterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class CC extends CountedCompleter<Void> {
        CC(CountedCompleter<?> parent) {
            super(parent);
        }
        public void compute() {
        }
    }

    static void testCountedCompleter() {
        CC root = new CC(null);
        CC child = new CC(root);
        check(root.getCompleter() == null, "root has no completer");
        check(child.getCompleter() == root, "child completer is the parent");
        check(child.getPendingCount() == 0, "initial pending count 0");
        child.addToPendingCount(2);
        check(child.getPendingCount() == 2, "addToPendingCount");
        child.tryComplete();
        check(child.getPendingCount() == 1, "tryComplete decrements pending");
        child.tryComplete();
        check(child.getPendingCount() == 0, "tryComplete reaches zero");
    }

    public static void main(String[] args) {
        testCountedCompleter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
