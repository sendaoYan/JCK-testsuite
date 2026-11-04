/*
 * @test
 * @summary java.util.concurrent.Phaser conformance: white-box structure + black-box
 *          behavior (register/arrive/advance/deregister/termination/party matrix,
 *          awaitAdvance variants, tiered phasers, unregistered arrival, forced
 *          termination releasing waiters).
 * @run main PhaserTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    static void testWhiteBoxExtra() throws Exception {
        Class<?> c = cls("java.util.concurrent.Phaser");
        hasMethod(c, "awaitAdvanceInterruptibly", int.class);
        hasMethod(c, "awaitAdvanceInterruptibly", int.class, long.class, TimeUnit.class);
        hasMethod(c, "getParent");
        hasMethod(c, "getRoot");
        check(new Phaser(2).toString().contains("parties = 2"), "toString reports the party count");
    }

    // ---- Part 2: black-box (behavioral) ----
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

    // ---- Part 3: contract deepening (appended) ----

    // a phaser created with no parties rejects arrivals of unregistered parties
    static void testUnregisteredArrival() {
        Phaser ph = new Phaser(); // zero registered parties
        check(ph.getRegisteredParties() == 0, "no-arg constructor registers zero parties");
        check(ph.getUnarrivedParties() == 0, "zero unarrived parties");
        try {
            ph.arrive();
            check(false, "arrive without registration should throw");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().contains("Attempted arrival of unregistered party"),
                  "arrive() ISE mentions unregistered party");
        }
        try {
            ph.arriveAndAwaitAdvance();
            check(false, "arriveAndAwaitAdvance without registration should throw");
        } catch (IllegalStateException e) {
            check(true, "arriveAndAwaitAdvance() of an unregistered party throws ISE");
        }
        try {
            ph.arriveAndDeregister();
            check(false, "arriveAndDeregister without registration should throw");
        } catch (IllegalStateException e) {
            check(true, "arriveAndDeregister() of an unregistered party throws ISE");
        }
        try {
            ph.bulkRegister(-1);
            check(false, "bulkRegister(-1) should throw");
        } catch (IllegalArgumentException e) {
            check(true, "bulkRegister(negative) throws IAE");
        }
        check(ph.bulkRegister(0) == 0, "bulkRegister(0) is a no-op returning the current phase");
        check(ph.getRegisteredParties() == 0, "bulkRegister(0) registers nothing");
    }

    // exact arrived/unarrived/registered matrix through a phase, plus mid-phase
    // register/bulkRegister effects and deregister shrinking later phases
    static void testPartyStateMatrix() {
        Phaser ph = new Phaser(3);
        check(ph.getPhase() == 0, "initial phase is 0");
        check(ph.getRegisteredParties() == 3, "three registered parties");
        check(ph.getArrivedParties() == 0, "none arrived");
        check(ph.getUnarrivedParties() == 3, "three unarrived");
        check(ph.arrive() == 0, "first arrive returns the arrival phase 0");
        check(ph.getArrivedParties() == 1, "one arrived");
        check(ph.getUnarrivedParties() == 2, "two unarrived");
        check(ph.arrive() == 0, "second arrive still in phase 0");
        check(ph.getArrivedParties() == 2 && ph.getUnarrivedParties() == 1, "matrix after two arrivals");
        check(ph.arriveAndAwaitAdvance() == 1, "last arrival trips and advances to phase 1");
        check(ph.getPhase() == 1, "phase is now 1");
        check(ph.getArrivedParties() == 0 && ph.getUnarrivedParties() == 3, "arrival counts reset for the new phase");
        ph.arriveAndDeregister(); // 3 parties, none arrived yet
        check(ph.getPhase() == 1, "deregistering one of three parties does not trip the phase");
        check(ph.getRegisteredParties() == 2, "deregister leaves two parties");
        check(ph.getArrivedParties() == 0 && ph.getUnarrivedParties() == 2, "matrix after deregister");
        ph.bulkRegister(3);
        check(ph.getRegisteredParties() == 5, "bulkRegister(3) adds parties mid-phase");
        check(ph.getUnarrivedParties() == 5, "newly registered parties join the current phase unarrived");
        check(ph.arrive() == 1, "arrive returns the arrival phase 1");
        ph.arrive();
        ph.arrive();
        ph.arrive();
        check(ph.arriveAndAwaitAdvance() == 2, "the fifth arrival trips phase 2");
        check(ph.getPhase() == 2, "phase 2");
        check(ph.getRegisteredParties() == 5 && ph.getArrivedParties() == 0 && ph.getUnarrivedParties() == 5,
              "matrix fully reset for phase 2");
    }

    static final class TerminatingPhaser extends Phaser {
        int lastPhase = -99, lastParties = -99, advanceCount;
        TerminatingPhaser(int parties) { super(parties); }
        protected boolean onAdvance(int phase, int registeredParties) {
            lastPhase = phase;
            lastParties = registeredParties;
            advanceCount++;
            return true; // terminate on the first advance
        }
    }

    // onAdvance returning true terminates; post-termination operations observe
    // negative phases and registrations have no effect (实测: no ISE from register)
    static void testOnAdvanceTermination() throws Exception {
        TerminatingPhaser ph = new TerminatingPhaser(2);
        check(ph.arrive() == 0, "first arrive returns phase 0");
        check(!ph.isTerminated(), "not terminated until the phase trips");
        check(ph.advanceCount == 0, "onAdvance not called before the trip");
        check(ph.arriveAndAwaitAdvance() == 1, "second party completes the phase, returns the new phase");
        check(ph.isTerminated(), "onAdvance returning true terminates the phaser");
        check(ph.advanceCount == 1, "onAdvance invoked exactly once");
        check(ph.lastPhase == 0, "onAdvance receives the completed phase");
        check(ph.lastParties == 2, "onAdvance receives the registered party count");
        check(ph.getPhase() < 0, "getPhase is negative once terminated");
        check(ph.getRegisteredParties() == 2, "termination does not deregister parties");
        check(ph.getArrivedParties() == 2, "arrived parties still reported after termination");
        check(ph.arrive() < 0, "arrive() on a terminated phaser returns a negative value");
        check(ph.arriveAndAwaitAdvance() < 0, "arriveAndAwaitAdvance() on a terminated phaser returns negative");
        check(ph.arriveAndDeregister() < 0, "arriveAndDeregister() on a terminated phaser returns negative");
        check(ph.awaitAdvance(0) < 0, "awaitAdvance on a terminated phaser returns negative");
        check(ph.awaitAdvanceInterruptibly(0) < 0, "awaitAdvanceInterruptibly on terminated returns negative");
        check(ph.register() < 0, "register() on a terminated phaser returns a negative phase");
        check(ph.getRegisteredParties() == 2, "register() on a terminated phaser has no effect");
        check(ph.bulkRegister(2) < 0, "bulkRegister(n>0) on a terminated phaser returns a negative phase");
        check(ph.getRegisteredParties() == 2, "bulkRegister on a terminated phaser has no effect");
    }

    // forceTermination releases a waiter which observes a negative phase; the
    // registered party count is unaffected
    static void testForceTerminationReleasesWaiter() throws Exception {
        final Phaser ph = new Phaser(2); // main + worker
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        Thread t = new Thread(() -> {
            entered.countDown();
            result.set(ph.arriveAndAwaitAdvance());
            done.countDown();
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker started");
        check(!ph.isTerminated(), "not terminated before forceTermination");
        ph.forceTermination();
        check(done.await(5, TimeUnit.SECONDS), "forceTermination releases the waiter (bounded)");
        t.join(5000);
        check(!t.isAlive(), "waiter finished");
        check(result.get() < 0, "released waiter observes a negative phase");
        check(ph.isTerminated(), "isTerminated after forceTermination");
        check(ph.getPhase() < 0, "phase is negative after forceTermination");
        check(ph.getRegisteredParties() == 2, "forceTermination does not change registered parties");
        check(ph.awaitAdvance(0) < 0, "awaitAdvance after termination returns negative");
        check(ph.register() < 0, "register() after forceTermination returns a negative phase");
    }

    // awaitAdvance blocks for the current phase and returns the new phase; a
    // passed phase returns immediately; a negative argument is returned as-is
    static void testAwaitAdvanceBlocking() throws Exception {
        final Phaser ph = new Phaser(1); // main owns the single party that trips phases
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(-99);
        Thread waiter = new Thread(() -> {
            entered.countDown();
            result.set(ph.awaitAdvance(0));
            done.countDown();
        });
        waiter.setDaemon(true);
        waiter.start();
        check(entered.await(5, TimeUnit.SECONDS), "waiter started");
        check(ph.arriveAndAwaitAdvance() == 1, "main trips the phase");
        check(done.await(5, TimeUnit.SECONDS), "awaitAdvance waiter released (bounded)");
        waiter.join(5000);
        check(!waiter.isAlive(), "waiter finished");
        check(result.get() == 1, "awaitAdvance(0) returns the new phase 1");
        check(ph.awaitAdvance(0) == 1, "awaitAdvance on an already-completed phase returns immediately");
        check(ph.awaitAdvance(-1) == -1, "awaitAdvance with a negative argument returns it as-is");
    }

    // awaitAdvanceInterruptibly: TimeoutException when the phase never advances,
    // immediate return for a passed phase, InterruptedException on interrupt
    static void testAwaitAdvanceInterruptiblyVariants() throws Exception {
        Phaser ph = new Phaser(1); // main owns the only party; phase never advances here
        try {
            ph.awaitAdvanceInterruptibly(0, 100, TimeUnit.MILLISECONDS);
            check(false, "awaitAdvanceInterruptibly should time out");
        } catch (TimeoutException e) {
            check(true, "awaitAdvanceInterruptibly times out with TimeoutException");
        }
        ph.arrive(); // phase 0 -> 1
        check(ph.awaitAdvanceInterruptibly(0) == 1, "already-passed phase returns immediately");
        check(ph.awaitAdvanceInterruptibly(0, 1, TimeUnit.SECONDS) == 1, "timed variant on a passed phase returns immediately");

        final Phaser ph2 = new Phaser(1); // nobody ever arrives on ph2
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                ph2.awaitAdvanceInterruptibly(0);
            } catch (InterruptedException e) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "waiter started");
        t.interrupt();
        check(done.await(5, TimeUnit.SECONDS), "interrupted awaitAdvanceInterruptibly returns promptly (bounded)");
        t.join(5000);
        check(!t.isAlive(), "interrupted waiter finished");
        check(interrupted.get(), "awaitAdvanceInterruptibly throws InterruptedException on interrupt");
    }

    // tiered phasers: the child's trip propagates to the parent, both advance
    static void testTieredPhaserAdvance() {
        Phaser parent = new Phaser();          // no direct parties of its own
        check(parent.getRegisteredParties() == 0, "parent registers no direct parties");
        check(parent.getParent() == null && parent.getRoot() == parent, "parent is its own root");
        Phaser child = new Phaser(parent, 1);  // registers the child with the parent
        check(parent.getRegisteredParties() == 1, "child construction registers it with the parent");
        check(child.getParent() == parent, "getParent returns the parent");
        check(child.getRoot() == parent, "getRoot returns the root");
        check(child.getPhase() == 0 && parent.getPhase() == 0, "both start at phase 0");
        check(child.arriveAndAwaitAdvance() == 1, "the child's only party trips the child");
        check(child.getPhase() == 1, "child advanced");
        check(parent.getPhase() == 1, "child trip propagates: the parent advanced too");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxExtra();
        testPhaser();
        testSingleParty();
        testRegisterDeregister();
        testTerminationOnZeroParties();
        testTwoThreadAdvance();
        testBlackBox();
        testUnregisteredArrival();
        testPartyStateMatrix();
        testOnAdvanceTermination();
        testForceTerminationReleasesWaiter();
        testAwaitAdvanceBlocking();
        testAwaitAdvanceInterruptiblyVariants();
        testTieredPhaserAdvance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
