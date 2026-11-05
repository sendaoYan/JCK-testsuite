/*
 * @test
 * @summary java.lang.ScopedValue conformance: white-box structure + black-box behavior.
 * @run main ScopedValueTest
 */

import java.lang.ScopedValue;
import java.util.concurrent.CountDownLatch;
import java.lang.reflect.Modifier;

public class ScopedValueTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ScopedValue");
        check(Modifier.isFinal(c.getModifiers()), "ScopedValue is final");
        check(!c.isInterface(), "ScopedValue is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUnbound() {
        ScopedValue<String> sv = ScopedValue.newInstance();
        check(!sv.isBound(), "not bound outside");
        check("d".equals(sv.orElse("d")), "orElse default");
        try {
            sv.get();
            throw new AssertionError("unbound get: expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            passed++;
        }
        try {
            sv.orElseThrow(() -> new IllegalStateException("unbound"));
            throw new AssertionError("orElseThrow: expected supplier exception");
        } catch (IllegalStateException expected) {
            passed++;
            check("unbound".equals(expected.getMessage()), "orElseThrow supplier message");
        }
        check(sv.hashCode() == sv.hashCode(), "hashCode stable");
    }

    static void testBound() {
        ScopedValue<String> sv = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(sv, "hello");
        check("hello".equals(car.get(sv)), "carrier get");
        check("hello".equals(car.call(() -> sv.get())), "carrier call");
        final int[] ran = {0};
        car.run(() -> { ran[0]++; check("hello".equals(sv.get()), "run sees value");
                        check(sv.isBound(), "bound inside run"); });
        check(ran[0] == 1, "run executed");
        ScopedValue.Carrier car2 = car.where(sv, "world");
        check("world".equals(car2.get(sv)), "where overrides");
        check("hello".equals(car.get(sv)), "original carrier unchanged");
        check(!sv.isBound(), "still unbound outside");
    }

    static void testWhereMultiple() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(a, "x").where(b, 3);
        check("x".equals(car.get(a)) && car.get(b) == 3, "chained where");
        car.run(() -> check("x".equals(a.get()) && b.get() == 3, "both values bound"));
    }

    
    static void testBlackBox() throws Exception {
        testUnbound();
        testBound();
        testWhereMultiple();
    }
    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    static void expectNSEE(ScopedValue<?> sv, String msg) {
        try {
            sv.get();
            throw new AssertionError(msg + ": expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            check("ScopedValue not bound".equals(expected.getMessage()),
                  msg + ": NoSuchElementException message exact");
        }
    }

    // where(KEY, v).run: value visible exactly inside the scope.
    static void testRunBindsExactValue() {
        ScopedValue<String> key = ScopedValue.newInstance();
        final String[] seen = {null};
        ScopedValue.where(key, "v").run(() -> {
            seen[0] = key.get();
            check("v".equals(key.get()), "get() returns the bound value exactly");
            check(key.isBound(), "isBound true inside run");
        });
        check("v".equals(seen[0]), "scope body ran and observed the value");
        check(!key.isBound(), "isBound false after run");
        expectNSEE(key, "get() after run");
    }

    // Defaults outside the scope.
    static void testAfterRunDefaults() {
        ScopedValue<Integer> key = ScopedValue.newInstance();
        ScopedValue.where(key, 1).run(() -> { });
        check(!key.isBound(), "unbound after run");
        expectNSEE(key, "get after scope");
        check(Integer.valueOf(9).equals(key.orElse(9)), "orElse default outside scope");
        try {
            key.orElseThrow(() -> new IllegalStateException("nope"));
            throw new AssertionError("orElseThrow outside scope: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            check("nope".equals(expected.getMessage()), "orElseThrow supplier exception outside scope");
        }
    }

    // Nested rebinding: inner value shadows, outer value restored on exit.
    static void testNestedRebinding() {
        ScopedValue<String> key = ScopedValue.newInstance();
        final String[] afterInner = {null};
        ScopedValue.where(key, "v1").run(() -> {
            check("v1".equals(key.get()), "outer value visible");
            ScopedValue.where(key, "v2").run(() -> {
                check("v2".equals(key.get()), "inner value shadows outer");
                check(key.isBound(), "bound in inner scope");
            });
            afterInner[0] = key.get();
            check("v1".equals(afterInner[0]), "outer value restored after inner scope");
        });
        check(!key.isBound(), "fully unbound after outer scope");
        expectNSEE(key, "get after all scopes");
    }

    // Inner scope exiting via exception still restores the outer binding.
    static void testNestedRebindingWithException() {
        ScopedValue<String> key = ScopedValue.newInstance();
        final String[] inFinally = {null};
        try {
            ScopedValue.where(key, "o").run(() -> {
                try {
                    ScopedValue.where(key, "i").run(() -> { throw new IllegalStateException("inner boom"); });
                } finally {
                    inFinally[0] = key.get();
                }
            });
            throw new AssertionError("inner exception should propagate");
        } catch (IllegalStateException expected) {
            check("inner boom".equals(expected.getMessage()), "inner exception propagates out of both scopes");
        }
        check("o".equals(inFinally[0]), "outer binding restored after inner scope threw");
        check(!key.isBound(), "unbound after exception unwound the outer scope");
    }

    // call() form: return value transparently passed through.
    static void testCallReturnsValue() throws Exception {
        ScopedValue<Integer> num = ScopedValue.newInstance();
        int doubled = ScopedValue.where(num, 5).call(() -> num.get() * 2);
        check(doubled == 10, "call() returns the callable's value");
        check(!num.isBound(), "unbound after call");
        String s = ScopedValue.where(num, 21).call(() -> "x" + num.get());
        check("x21".equals(s), "call() with a different callable");
    }

    static class CheckedException extends Exception { CheckedException(String m) { super(m); } }

    static void testCallCheckedException() {
        ScopedValue<Integer> num = ScopedValue.newInstance();
        try {
            ScopedValue.where(num, 1).call(() -> { throw new CheckedException("checked"); });
            throw new AssertionError("call with checked exception should propagate");
        } catch (CheckedException expected) {
            check("checked".equals(expected.getMessage()), "checked exception propagates from call");
        }
        check(!num.isBound(), "unbound after call threw");
    }

    // run() rethrows the exact exception instance thrown by the body.
    static void testRunExceptionSameInstance() {
        ScopedValue<String> key = ScopedValue.newInstance();
        RuntimeException boom = new RuntimeException("boom");
        try {
            ScopedValue.where(key, "v").run(() -> { throw boom; });
            throw new AssertionError("run should rethrow");
        } catch (RuntimeException re) {
            check(re == boom, "run rethrows the identical exception instance");
        }
        check(!key.isBound(), "binding released when the body throws");
        expectNSEE(key, "get after throwing scope");
    }

    // Chained where on multiple independent keys.
    static void testWhereChainMultipleKeys() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue<Boolean> c = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(a, "x").where(b, 3).where(c, true);
        check(car != null, "chained where returns a carrier");
        car.run(() -> {
            check("x".equals(a.get()), "first key bound");
            check(Integer.valueOf(3).equals(b.get()), "second key bound");
            check(Boolean.TRUE.equals(c.get()), "third key bound");
            check(a.isBound() && b.isBound() && c.isBound(), "all keys isBound");
        });
        check(!a.isBound() && !b.isBound() && !c.isBound(), "all keys unbound after run");
        // rebinding one key of the chain inside a nested scope keeps the others
        ScopedValue.where(a, "outer-a").where(b, 7).run(() -> {
            ScopedValue.where(a, "inner-a").run(() -> {
                check("inner-a".equals(a.get()), "rebound key sees inner value");
                check(Integer.valueOf(7).equals(b.get()), "untouched key keeps outer value");
            });
        });
    }

    // One carrier may be reused for several independent runs/calls.
    static void testCarrierReuse() {
        ScopedValue<String> key = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(key, "r");
        final int[] runs = {0};
        car.run(() -> runs[0]++);
        check(runs[0] == 1, "first run executed");
        check(!key.isBound(), "unbound between carrier runs");
        car.run(() -> runs[0]++);
        check(runs[0] == 2, "second run executed with the same carrier");
        check("r".equals(car.call(() -> key.get())), "carrier call sees the value");
        check("r".equals(car.get(key)), "carrier get outside operation still works");
        check(!key.isBound(), "still unbound after all uses");
    }

    // Thread confinement (signature closure): a binding made by one thread is
    // invisible to another thread, and vice versa.
    static class Child extends Thread {
        final CountDownLatch done = new CountDownLatch(1);
        boolean childSeesBound;
        boolean childGetThrew;
        String childLocalValue;
        Throwable failure;
        Child() { super("lcI-sv-child"); }
        public void run() {
            try {
                ScopedValue<String> mainKey = mainThreadKey;
                childSeesBound = mainKey.isBound();
                try {
                    mainKey.get();
                    childGetThrew = false;
                } catch (java.util.NoSuchElementException expected) {
                    childGetThrew = true;
                }
                ScopedValue<String> own = ScopedValue.newInstance();
                ScopedValue.where(own, "child-only").run(() -> childLocalValue = own.get());
            } catch (Throwable t) {
                failure = t;
            } finally {
                done.countDown();
            }
        }
    }
    static ScopedValue<String> mainThreadKey;

    static void testThreadConfinement() throws Exception {
        ScopedValue<String> key = ScopedValue.newInstance();
        mainThreadKey = key;
        Child child = new Child();
        ScopedValue.where(key, "main-only").run(() -> {
            child.start();
            boolean finished = false;
            try {
                finished = child.done.await(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            check(finished, "child finished while main binding active (bounded latch)");
            check(!child.childSeesBound, "child thread does not see the main thread's binding");
            check(child.childGetThrew, "child get() throws NoSuchElementException");
            check("child-only".equals(child.childLocalValue), "child can bind its own value");
            check(child.failure == null, "child observed no failure");
            check("main-only".equals(key.get()), "main binding unaffected by child activity");
        });
        check(!key.isBound(), "main binding released after scope");
        child.join(30000);
    }

    // A null value is a legitimate binding (distinct from unbound).
    static void testNullValueBinding() {
        ScopedValue<String> key = ScopedValue.newInstance();
        ScopedValue.where(key, null).run(() -> {
            check(key.isBound(), "null value counts as bound");
            check(key.get() == null, "get returns null for a null binding");
            check(key.orElse("d") == null, "orElse returns the null binding, not the default");
        });
        check(!key.isBound(), "unbound after null-value scope");
        expectNSEE(key, "get after null-value scope (null binding is not unbound)");
    }

    // newInstance(): every key is independent.
    static void testNewInstanceIndependence() {
        ScopedValue<String> k1 = ScopedValue.newInstance();
        ScopedValue<String> k2 = ScopedValue.newInstance();
        check(k1 != k2, "newInstance returns distinct keys");
        ScopedValue.where(k1, "one").run(() -> {
            check("one".equals(k1.get()), "k1 bound");
            check(!k2.isBound(), "k2 unaffected by k1 binding");
        });
        ScopedValue.where(k2, "two").run(() -> check("two".equals(k2.get()), "k2 bindable independently"));
        check(k1.equals(k1) && !k1.equals(k2), "identity equals semantics for key instances");
        check(k1.hashCode() == k1.hashCode(), "key hashCode stable");
        check(k1.toString() != null, "key toString non-null");
    }

    // orElse / orElseThrow inside vs outside the scope.
    static void testOrElseInsideScope() {
        ScopedValue<String> key = ScopedValue.newInstance();
        ScopedValue.where(key, "bound").run(() -> {
            check("bound".equals(key.orElse("other")), "orElse returns binding inside scope");
            check("bound".equals(key.orElseThrow(() -> new IllegalStateException("nope"))),
                  "orElseThrow returns binding inside scope");
        });
        check("other".equals(key.orElse("other")), "orElse default outside scope");
    }

    // Rebinding the same key to the same value in a nested scope is legal.
    static void testSameValueRebind() {
        ScopedValue<String> key = ScopedValue.newInstance();
        final boolean[] innerOk = {false};
        ScopedValue.where(key, "s").run(() ->
            ScopedValue.where(key, "s").run(() -> innerOk[0] = "s".equals(key.get())));
        check(innerOk[0], "same-value rebind works");
        check(!key.isBound(), "unbound after same-value rebind test");
    }

    // where(null, v) must reject a null key.
    static void testWhereNullKey() {
        try {
            ScopedValue.where(null, "v");
            throw new AssertionError("where(null, v): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "where(null, v) -> NullPointerException");
        }
    }

    // Three-level rebinding: each level sees its own value and unwinds correctly.
    static void testThreeLevelRebinding() {
        ScopedValue<Integer> key = ScopedValue.newInstance();
        final String[] trail = {""};
        ScopedValue.where(key, 1).run(() ->
            ScopedValue.where(key, 2).run(() ->
                ScopedValue.where(key, 3).run(() ->
                    trail[0] += key.get())));
        check("3".equals(trail[0]), "innermost level sees 3");
        final int[] afterInner = {0};
        ScopedValue.where(key, 1).run(() -> {
            ScopedValue.where(key, 2).run(() -> { });
            afterInner[0] = key.get();
        });
        check(afterInner[0] == 1, "two levels unwind to the outer value");
        final boolean[] boundAtDeepest = {false};
        ScopedValue.where(key, 1).run(() ->
            ScopedValue.where(key, 2).run(() ->
                boundAtDeepest[0] = key.isBound()));
        check(boundAtDeepest[0], "isBound true at the deepest level");
        check(!key.isBound(), "unbound after three-level test");
    }

    // Both keys bound at once through one carrier, read through carrier.get.
    static void testCarrierGetMultiple() {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<String> b = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(a, "A").where(b, "B");
        check("A".equals(car.get(a)), "carrier.get first key");
        check("B".equals(car.get(b)), "carrier.get second key");
        final String[] inside = {null};
        car.run(() -> inside[0] = a.get() + b.get());
        check("AB".equals(inside[0]), "both values visible inside run");
        check(!a.isBound() && !b.isBound(), "both unbound after run");
        ScopedValue.Carrier car2 = car.where(a, "A2");
        check("A2".equals(car2.get(a)), "carrier.where returns a new carrier with override");
        check("A".equals(car.get(a)), "original carrier unchanged by where");
    }

    // A carrier still works after a run whose body threw.
    static void testCarrierSurvivesBodyFailure() {
        ScopedValue<String> key = ScopedValue.newInstance();
        ScopedValue.Carrier car = ScopedValue.where(key, "ok");
        try {
            car.run(() -> { throw new UnsupportedOperationException("body"); });
        } catch (UnsupportedOperationException expected) {
            check("body".equals(expected.getMessage()), "body failure propagates");
        }
        check(!key.isBound(), "unbound after failing run");
        final boolean[] ok = {false};
        car.run(() -> ok[0] = "ok".equals(key.get()));
        check(ok[0], "carrier usable again after a failing run");
        check("ok".equals(car.call(() -> key.get())), "carrier call usable after failure");
    }

    // Child's own binding is gone when the child's scope ends, and is invisible to main.
    static void testChildBindingInvisible() throws Exception {
        final CountDownLatch done = new CountDownLatch(1);
        final boolean[] childBoundAfterScope = {false};
        ScopedValue<String> childKey = ScopedValue.newInstance();
        Thread t = new Thread(() -> {
            ScopedValue.where(childKey, "child").run(() -> { });
            childBoundAfterScope[0] = childKey.isBound();
            done.countDown();
        }, "lcI-sv-child2");
        t.start();
        check(done.await(30, java.util.concurrent.TimeUnit.SECONDS), "child2 finished (bounded latch)");
        check(!childBoundAfterScope[0], "child's binding released at its scope end");
        check(!childKey.isBound(), "child's binding invisible in the main thread");
        t.join(30000);
    }

    static void testDeepenedContract() throws Exception {
        testThreeLevelRebinding();
        testCarrierGetMultiple();
        testCarrierSurvivesBodyFailure();
        testChildBindingInvisible();
        testRunBindsExactValue();
        testAfterRunDefaults();
        testNestedRebinding();
        testNestedRebindingWithException();
        testCallReturnsValue();
        testCallCheckedException();
        testRunExceptionSameInstance();
        testWhereChainMultipleKeys();
        testCarrierReuse();
        testThreadConfinement();
        testNullValueBinding();
        testNewInstanceIndependence();
        testOrElseInsideScope();
        testSameValueRebind();
        testWhereNullKey();
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepenedContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
