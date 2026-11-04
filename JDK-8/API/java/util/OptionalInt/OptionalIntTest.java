/*
 * @test
 * @summary java.util.OptionalInt API conformance (Java 8 surface): complete contract
 *          coverage of of/empty, isPresent/getAsInt, ifPresent, orElse/orElseGet/orElseThrow,
 *          equals/hashCode/toString, boundary values, and null supplier/consumer branches.
 * @run main OptionalIntTest
 */

import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OptionalIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testOfGet() {
        OptionalInt present = OptionalInt.of(5);
        check(present.isPresent() && present.getAsInt() == 5, "of/isPresent/getAsInt");
        OptionalInt empty = OptionalInt.empty();
        check(!empty.isPresent(), "empty isPresent");
        expectThrows(NoSuchElementException.class, () -> empty.getAsInt(),
            "empty getAsInt NoSuchElementException");
    }

    static void testBoundaries() {
        check(OptionalInt.of(Integer.MIN_VALUE).getAsInt() == Integer.MIN_VALUE, "of(MIN)");
        check(OptionalInt.of(Integer.MAX_VALUE).getAsInt() == Integer.MAX_VALUE, "of(MAX)");
        check(OptionalInt.of(0).getAsInt() == 0, "of(0)");
        check(OptionalInt.of(-1).getAsInt() == -1, "of(-1)");
    }

    static void testOrElse() {
        OptionalInt present = OptionalInt.of(5);
        OptionalInt empty = OptionalInt.empty();

        check(present.orElse(9) == 5, "present orElse");
        check(empty.orElse(9) == 9, "empty orElse");

        final AtomicBoolean called = new AtomicBoolean(false);
        check(present.orElseGet(() -> { called.set(true); return 9; }) == 5, "present orElseGet");
        check(!called.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 9) == 9, "empty orElseGet");

        check(present.orElseGet(null) == 5, "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseGet(null),
            "orElseGet(null) empty NPE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 5,
              "present orElseThrow(supplier)");
        check(present.orElseThrow(null) == 5, "orElseThrow(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseThrow(null),
            "orElseThrow(null) empty NPE");
        expectThrows(NullPointerException.class, () -> empty.orElseThrow(() -> null),
            "orElseThrow supplier returns null NPE");

        IllegalStateException ex = new IllegalStateException("boom");
        try {
            empty.orElseThrow(() -> ex);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException caught) {
            check(caught == ex, "orElseThrow passes through same exception object");
        }
    }

    static void testIfPresent() {
        OptionalInt present = OptionalInt.of(5);
        OptionalInt empty = OptionalInt.empty();

        final AtomicInteger consumed = new AtomicInteger(0);
        present.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 5, "ifPresent invokes IntConsumer");
        empty.ifPresent(v -> consumed.set(-1));
        check(consumed.get() == 5, "ifPresent on empty does nothing");

        expectThrows(NullPointerException.class, () -> present.ifPresent(null),
            "ifPresent(null) present NPE");
        empty.ifPresent(null);
        passed++; // documented no-op for empty + null consumer
    }

    static void testEqualsHashCodeToString() {
        check(OptionalInt.of(5).equals(OptionalInt.of(5)), "equals present");
        check(OptionalInt.empty().equals(OptionalInt.empty()), "equals empty");
        check(!OptionalInt.of(5).equals(OptionalInt.of(6)), "!equals different");
        check(!OptionalInt.of(5).equals(OptionalInt.empty()), "!equals present vs empty");
        check(!OptionalInt.empty().equals(OptionalInt.of(5)), "!equals empty vs present");
        check(OptionalInt.of(5).hashCode() == OptionalInt.of(5).hashCode(), "hashCode stable");
        check(OptionalInt.empty().hashCode() == OptionalInt.empty().hashCode(), "empty hashCode stable");
        check(OptionalInt.of(5).toString().equals("OptionalInt[5]"), "toString present");
        check(OptionalInt.empty().toString().equals("OptionalInt.empty"), "toString empty");
    }

    public static void main(String[] args) {
        testOfGet();
        testBoundaries();
        testOrElse();
        testIfPresent();
        testEqualsHashCodeToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
