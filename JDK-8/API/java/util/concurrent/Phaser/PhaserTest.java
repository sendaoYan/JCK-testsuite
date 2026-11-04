/*
 * @test
 * @summary java.util.concurrent.Phaser API conformance: phase advancement with a single
 *          registered party.
 * @run main PhaserTest
 */

import java.util.concurrent.Phaser;

public class PhaserTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPhaser() {
        Phaser ph = new Phaser(1);
        check(ph.getRegisteredParties() == 1, "one registered party");
        check(ph.getPhase() == 0, "initial phase 0");
        // arriveAndAwaitAdvance returns the phase number advanced TO, not the
        // phase the party arrived on.
        check(ph.arriveAndAwaitAdvance() == 1, "arriveAndAwaitAdvance advances to 1");
        check(ph.getPhase() == 1, "phase advanced to 1");
        check(ph.arrive() == 1, "arrive returns current phase 1");
    }

    public static void main(String[] args) {
        testPhaser();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
