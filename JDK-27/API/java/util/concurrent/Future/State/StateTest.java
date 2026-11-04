/*
 * @test
 * @summary java.util.concurrent.Future$State conformance: white-box structure + black-box behavior.
 * @run main StateTest
 */

import java.util.concurrent.*;

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Future$State");
        check(c.isEnum(), "Future.State is an enum");
        hasMethod(c, "values");
        hasMethod(c, "valueOf", String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Future.State.values().length == 4, "four states");
        check(Future.State.valueOf("RUNNING") == Future.State.RUNNING, "valueOf RUNNING");
        check(Future.State.valueOf("SUCCESS") == Future.State.SUCCESS, "valueOf SUCCESS");
        check(Future.State.valueOf("FAILED") == Future.State.FAILED, "valueOf FAILED");
        check(Future.State.valueOf("CANCELLED") == Future.State.CANCELLED, "valueOf CANCELLED");
        CompletableFuture<Integer> running = new CompletableFuture<>();
        check(running.state() == Future.State.RUNNING, "incomplete future is RUNNING");
        check(CompletableFuture.completedFuture(1).state() == Future.State.SUCCESS, "completed is SUCCESS");
        check(CompletableFuture.failedFuture(new Exception()).state() == Future.State.FAILED, "failed is FAILED");
        CompletableFuture<Integer> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);
        check(cancelled.state() == Future.State.CANCELLED, "cancelled is CANCELLED");
    }

    // ---- Part 3: enum intrinsic contract (order/ordinal/name/valueOf/cloneable array) ----
    static void testEnumContract() {
        Future.State[] vals = Future.State.values();
        check(vals.length == 4, "exactly four constants");
        // declaration order locked: RUNNING, SUCCESS, FAILED, CANCELLED
        check(vals[0] == Future.State.RUNNING && vals[0].ordinal() == 0, "ordinal 0 RUNNING");
        check(vals[1] == Future.State.SUCCESS && vals[1].ordinal() == 1, "ordinal 1 SUCCESS");
        check(vals[2] == Future.State.FAILED && vals[2].ordinal() == 2, "ordinal 2 FAILED");
        check(vals[3] == Future.State.CANCELLED && vals[3].ordinal() == 3, "ordinal 3 CANCELLED");
        check("RUNNING".equals(Future.State.RUNNING.name()), "name RUNNING");
        check("SUCCESS".equals(Future.State.SUCCESS.name()), "name SUCCESS");
        check("FAILED".equals(Future.State.FAILED.name()), "name FAILED");
        check("CANCELLED".equals(Future.State.CANCELLED.name()), "name CANCELLED");
        check(Future.State.valueOf("RUNNING") == Future.State.RUNNING, "valueOf round-trip RUNNING");
        check(Future.State.valueOf("SUCCESS") == Future.State.SUCCESS, "valueOf round-trip SUCCESS");
        check(Future.State.valueOf("FAILED") == Future.State.FAILED, "valueOf round-trip FAILED");
        check(Future.State.valueOf("CANCELLED") == Future.State.CANCELLED,
              "valueOf round-trip CANCELLED");
        check(Enum.valueOf(Future.State.class, "FAILED") == Future.State.FAILED,
              "Enum.valueOf works");
        // natural order is ordinal order (Comparable)
        check(Future.State.RUNNING.compareTo(Future.State.SUCCESS) < 0, "RUNNING < SUCCESS");
        check(Future.State.CANCELLED.compareTo(Future.State.FAILED) > 0, "CANCELLED > FAILED");
        check(Future.State.SUCCESS.compareTo(Future.State.SUCCESS) == 0, "compareTo equal");
        // identity/equals/hashCode are enum-singleton semantics
        check(Future.State.RUNNING == Future.State.valueOf("RUNNING"), "singleton identity");
        check(Future.State.RUNNING.hashCode() == Future.State.RUNNING.hashCode(), "stable hashCode");
        check(Future.State.RUNNING.getDeclaringClass() == Future.State.class,
              "getDeclaringClass is Future.State");
        // values() returns a fresh, mutable copy each time
        Future.State[] first = Future.State.values();
        first[0] = null;
        check(Future.State.values()[0] == Future.State.RUNNING,
              "values() returns a defensive copy");
        // bogus name -> IllegalArgumentException
        boolean iae = false;
        try { Future.State.valueOf("bogus"); }
        catch (IllegalArgumentException expected) { iae = true; }
        check(iae, "valueOf(bogus) throws IllegalArgumentException");
        boolean npe = false;
        try { Future.State.valueOf(null); }
        catch (NullPointerException expected) { npe = true; }
        check(npe, "valueOf(null) throws NullPointerException");
        check(Future.State.values()[0].toString().equals("RUNNING"), "toString is the name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
