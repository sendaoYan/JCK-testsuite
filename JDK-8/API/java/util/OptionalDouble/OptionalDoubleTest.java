/*
 * @test
 * @summary java.util.OptionalDouble API conformance (Java 8 surface): complete contract
 *          coverage of of/empty, isPresent/getAsDouble, ifPresent, orElse/orElseGet/orElseThrow,
 *          equals/hashCode/toString (Double.compare/NaN/±0.0 semantics), boundary values,
 *          and null supplier/consumer branches.
 * @run main OptionalDoubleTest
 */

import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptionalDoubleTest {
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
        OptionalDouble present = OptionalDouble.of(2.5);
        check(present.isPresent() && present.getAsDouble() == 2.5, "of/isPresent/getAsDouble");
        OptionalDouble empty = OptionalDouble.empty();
        check(!empty.isPresent(), "empty isPresent");
        expectThrows(NoSuchElementException.class, () -> empty.getAsDouble(),
            "empty getAsDouble NoSuchElementException");
    }

    static void testBoundaries() {
        check(OptionalDouble.of(0.0).getAsDouble() == 0.0, "of(0.0)");
        check(Double.compare(OptionalDouble.of(-0.0).getAsDouble(), -0.0) == 0, "of(-0.0)");
        check(Double.isNaN(OptionalDouble.of(Double.NaN).getAsDouble()), "of(NaN)");
        check(OptionalDouble.of(Double.POSITIVE_INFINITY).getAsDouble()
              == Double.POSITIVE_INFINITY, "of(+Inf)");
        check(OptionalDouble.of(Double.NEGATIVE_INFINITY).getAsDouble()
              == Double.NEGATIVE_INFINITY, "of(-Inf)");
        check(OptionalDouble.of(Double.MIN_VALUE).getAsDouble() == Double.MIN_VALUE, "of(MIN_VALUE)");
    }

    static void testOrElse() {
        OptionalDouble present = OptionalDouble.of(2.5);
        OptionalDouble empty = OptionalDouble.empty();

        check(present.orElse(9.0) == 2.5, "present orElse");
        check(empty.orElse(9.0) == 9.0, "empty orElse");

        final AtomicBoolean called = new AtomicBoolean(false);
        check(present.orElseGet(() -> { called.set(true); return 9.0; }) == 2.5, "present orElseGet");
        check(!called.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 9.0) == 9.0, "empty orElseGet");

        check(present.orElseGet(null) == 2.5, "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseGet(null),
            "orElseGet(null) empty NPE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 2.5,
              "present orElseThrow(supplier)");
        check(present.orElseThrow(null) == 2.5, "orElseThrow(null) present returns value");
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
        OptionalDouble present = OptionalDouble.of(2.5);
        OptionalDouble empty = OptionalDouble.empty();

        final double[] consumed = {0.0};
        present.ifPresent(v -> consumed[0] = v);
        check(consumed[0] == 2.5, "ifPresent invokes DoubleConsumer");
        empty.ifPresent(v -> consumed[0] = -1.0);
        check(consumed[0] == 2.5, "ifPresent on empty does nothing");

        expectThrows(NullPointerException.class, () -> present.ifPresent(null),
            "ifPresent(null) present NPE");
        empty.ifPresent(null);
        passed++; // documented no-op for empty + null consumer
    }

    static void testEqualsHashCodeToString() {
        check(OptionalDouble.of(2.5).equals(OptionalDouble.of(2.5)), "equals present");
        check(OptionalDouble.empty().equals(OptionalDouble.empty()), "equals empty");
        check(!OptionalDouble.of(2.5).equals(OptionalDouble.of(3.5)), "!equals different");
        check(!OptionalDouble.of(2.5).equals(OptionalDouble.empty()), "!equals present vs empty");
        check(!OptionalDouble.empty().equals(OptionalDouble.of(2.5)), "!equals empty vs present");
        check(OptionalDouble.of(2.5).hashCode() == OptionalDouble.of(2.5).hashCode(), "hashCode stable");
        check(OptionalDouble.empty().hashCode() == OptionalDouble.empty().hashCode(), "empty hashCode stable");

        // equals uses Double.compare: NaN equals NaN, +0.0 != -0.0.
        check(OptionalDouble.of(Double.NaN).equals(OptionalDouble.of(Double.NaN)), "equals NaN==NaN");
        check(!OptionalDouble.of(0.0).equals(OptionalDouble.of(-0.0)), "equals +0.0 != -0.0");

        check(OptionalDouble.of(2.5).toString().equals("OptionalDouble[2.5]"), "toString present");
        check(OptionalDouble.empty().toString().equals("OptionalDouble.empty"), "toString empty");
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
