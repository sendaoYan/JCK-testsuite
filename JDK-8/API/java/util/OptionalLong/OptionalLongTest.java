/*
 * @test
 * @summary java.util.OptionalLong API conformance (Java 8 surface): complete contract
 *          coverage of of/empty, isPresent/getAsLong, ifPresent, orElse/orElseGet/orElseThrow,
 *          equals/hashCode/toString, boundary values, and null supplier/consumer branches.
 * @run main OptionalLongTest
 */

import java.util.NoSuchElementException;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class OptionalLongTest {
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
        OptionalLong present = OptionalLong.of(10L);
        check(present.isPresent() && present.getAsLong() == 10L, "of/isPresent/getAsLong");
        OptionalLong empty = OptionalLong.empty();
        check(!empty.isPresent(), "empty isPresent");
        expectThrows(NoSuchElementException.class, () -> empty.getAsLong(),
            "empty getAsLong NoSuchElementException");
    }

    static void testBoundaries() {
        check(OptionalLong.of(Long.MIN_VALUE).getAsLong() == Long.MIN_VALUE, "of(MIN)");
        check(OptionalLong.of(Long.MAX_VALUE).getAsLong() == Long.MAX_VALUE, "of(MAX)");
        check(OptionalLong.of(0L).getAsLong() == 0L, "of(0)");
        check(OptionalLong.of(-1L).getAsLong() == -1L, "of(-1)");
    }

    static void testOrElse() {
        OptionalLong present = OptionalLong.of(10L);
        OptionalLong empty = OptionalLong.empty();

        check(present.orElse(99L) == 10L, "present orElse");
        check(empty.orElse(99L) == 99L, "empty orElse");

        final AtomicBoolean called = new AtomicBoolean(false);
        check(present.orElseGet(() -> { called.set(true); return 99L; }) == 10L, "present orElseGet");
        check(!called.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 99L) == 99L, "empty orElseGet");

        check(present.orElseGet(null) == 10L, "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseGet(null),
            "orElseGet(null) empty NPE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 10L,
              "present orElseThrow(supplier)");
        check(present.orElseThrow(null) == 10L, "orElseThrow(null) present returns value");
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
        OptionalLong present = OptionalLong.of(10L);
        OptionalLong empty = OptionalLong.empty();

        final AtomicLong consumed = new AtomicLong(0L);
        present.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 10L, "ifPresent invokes LongConsumer");
        empty.ifPresent(v -> consumed.set(-1L));
        check(consumed.get() == 10L, "ifPresent on empty does nothing");

        expectThrows(NullPointerException.class, () -> present.ifPresent(null),
            "ifPresent(null) present NPE");
        empty.ifPresent(null);
        passed++; // documented no-op for empty + null consumer
    }

    static void testEqualsHashCodeToString() {
        check(OptionalLong.of(10L).equals(OptionalLong.of(10L)), "equals present");
        check(OptionalLong.empty().equals(OptionalLong.empty()), "equals empty");
        check(!OptionalLong.of(10L).equals(OptionalLong.of(11L)), "!equals different");
        check(!OptionalLong.of(10L).equals(OptionalLong.empty()), "!equals present vs empty");
        check(!OptionalLong.empty().equals(OptionalLong.of(10L)), "!equals empty vs present");
        check(OptionalLong.of(10L).hashCode() == OptionalLong.of(10L).hashCode(), "hashCode stable");
        check(OptionalLong.empty().hashCode() == OptionalLong.empty().hashCode(), "empty hashCode stable");
        check(OptionalLong.of(10L).toString().equals("OptionalLong[10]"), "toString present");
        check(OptionalLong.empty().toString().equals("OptionalLong.empty"), "toString empty");
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
