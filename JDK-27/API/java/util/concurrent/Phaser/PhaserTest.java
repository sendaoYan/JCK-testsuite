/*
 * @test
 * @summary java.util.concurrent.Phaser conformance: white-box structure + black-box
 *          behavior (register/arrive/advance/deregister/termination).
 * @run main PhaserTest
 */

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class PhaserTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Phaser");
        check(!c.isInterface(), "Phaser is a class");
        hasMethod(c, "register");
        hasMethod(c, "bulkRegister", int.class);
        hasMethod(c, "arrive");
        hasMethod(c, "arriveAndDeregister");
        hasMethod(c, "arriveAndAwaitAdvance");
        hasMethod(c, "awaitAdvance", int.class);
        hasMethod(c, "getPhase");
        hasMethod(c, "getRegisteredParties");
        hasMethod(c, "getArrivedParties");
        hasMethod(c, "getUnarrivedParties");
        hasMethod(c, "forceTermination");
        hasMethod(c, "isTerminated");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSingleParty() {
        Phaser ph = new Phaser(1);
        check(ph.getRegisteredParties() == 1, "one registered party");
        check(ph.getPhase() == 0, "initial phase 0");
        check(ph.getArrivedParties() == 0, "no arrived parties");
        check(ph.getUnarrivedParties() == 1, "one unarrived party");
        // arriveAndAwaitAdvance returns the phase advanced TO
        check(ph.arriveAndAwaitAdvance() == 1, "arriveAndAwaitAdvance advances to 1");
        check(ph.getPhase() == 1, "phase advanced to 1");
        check(ph.arrive() == 1, "arrive returns current phase 1");
    }

    static void testRegisterDeregister() {
        Phaser ph = new Phaser();
        check(ph.getRegisteredParties() == 0, "zero parties initially");
        check(ph.register() == 0, "register returns current phase");
        check(ph.getRegisteredParties() == 1, "one party after register");
        ph.bulkRegister(2);
        check(ph.getRegisteredParties() == 3, "three parties after bulkRegister(2)");
        check(ph.arriveAndDeregister() == 0, "arriveAndDeregister returns current phase");
        check(ph.getRegisteredParties() == 2, "deregister removes one party");
        ph.forceTermination();
        check(ph.isTerminated(), "forceTermination terminates the phaser");
    }

    static void testTerminationOnZeroParties() {
        Phaser one = new Phaser(1);
        check(one.arriveAndDeregister() == 0, "single party deregisters");
        check(one.isTerminated(), "terminates when parties reach zero (default onAdvance)");
    }

    static void testTwoThreadAdvance() throws Exception {
        Phaser ph = new Phaser(1);
        ph.register(); // two registered parties now
        final int[] workerResult = new int[1];
        Thread t = new Thread(() -> workerResult[0] = ph.arriveAndAwaitAdvance());
        t.start();
        int mainResult = ph.arriveAndAwaitAdvance();
        t.join(5000);
        check(!t.isAlive(), "worker finished");
        check(ph.getPhase() == 1, "phase advanced to 1");
        check(mainResult == 1 && workerResult[0] == 1, "both parties advance to phase 1");
    }

    static class RecordingPhaser extends Phaser {
        boolean advanced;
        RecordingPhaser(int parties) { super(parties); }
        protected boolean onAdvance(int phase, int registeredParties) {
            advanced = true;
            return super.onAdvance(phase, registeredParties);
        }
    }

    static void testBlackBox() throws Exception {
        // constructors (Phaser)/(Phaser,int) + getParent/getRoot
        Phaser root = new Phaser(1);
        Phaser child = new Phaser(root, 1);
        check(child.getParent() == root, "getParent returns parent");
        check(child.getRoot() == root && root.getRoot() == root, "getRoot returns root");
        Phaser child2 = new Phaser(root);
        check(child2.getParent() == root, "constructor(Phaser) registers as child");

        // onAdvance override invoked on phase advance
        RecordingPhaser rp = new RecordingPhaser(1);
        check(!rp.advanced, "onAdvance not yet called");
        rp.arriveAndDeregister();
        check(rp.advanced, "onAdvance called on advance");
        check(rp.isTerminated(), "terminated after deregister");

        // awaitAdvanceInterruptibly(int) / timed variant for an already-completed phase
        Phaser p = new Phaser(1);
        int phase = p.arrive(); // completes phase 0, advances to 1
        check(phase == 0, "arrive returns arrival phase 0");
        check(p.awaitAdvanceInterruptibly(0) == 1, "awaitAdvanceInterruptibly(0) returns current phase");
        check(p.awaitAdvanceInterruptibly(0, 1, TimeUnit.NANOSECONDS) == 1,
              "awaitAdvanceInterruptibly(0,1,ns) returns current phase");

        // toString
        check(p.toString() != null && p.toString().length() > 0, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleParty();
        testRegisterDeregister();
        testTerminationOnZeroParties();
        testTwoThreadAdvance();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
