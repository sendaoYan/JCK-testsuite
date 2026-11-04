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

    // ============ contract deepening (appended) ============

    // NSEE message text is observable contract surface.
    static void testExceptionMessages() {
        try {
            OptionalDouble.empty().getAsDouble();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty getAsDouble NSEE message");
        }
    }

    // hashCode() == (isPresent) ? Double.hashCode(value) : 0; the full bit-pattern formula.
    static void testHashCodeFormula() {
        check(OptionalDouble.empty().hashCode() == 0, "empty hashCode == 0");
        check(OptionalDouble.of(0.0).hashCode() == 0, "of(0.0) hashCode == 0");
        // Double.hashCode(-0.0) == Integer.MIN_VALUE (sign bit only in high word)
        check(OptionalDouble.of(-0.0).hashCode() == Integer.MIN_VALUE, "of(-0.0) hashCode == MIN_VALUE");
        // Double.hashCode(1.0) == 0x3FF00000
        check(OptionalDouble.of(1.0).hashCode() == 1072693248, "of(1.0) hashCode == 1072693248");
        check(OptionalDouble.of(Double.NaN).hashCode() == Double.hashCode(Double.NaN),
              "of(NaN) hashCode == Double.hashCode(NaN)");
        check(OptionalDouble.of(Double.NaN).hashCode() == OptionalDouble.of(Double.NaN).hashCode(),
              "NaN hashCode stable");
        // +0.0 and -0.0 are not equal and hash differently
        check(OptionalDouble.of(0.0).hashCode() != OptionalDouble.of(-0.0).hashCode(),
              "hashCode distinguishes +0.0 from -0.0");
        check(OptionalDouble.of(2.5).hashCode() == Double.hashCode(2.5), "delegates to Double.hashCode");
    }

    // equals only compares against OptionalDouble; NaN equals NaN, +0.0 != -0.0.
    static void testEqualsCrossType() {
        check(!OptionalDouble.of(2.5).equals(null), "equals(null) false");
        check(!OptionalDouble.of(2.5).equals(Double.valueOf(2.5)), "not equal to plain Double");
        check(!OptionalDouble.of(2.5).equals(java.util.Optional.of(2.5)), "not equal to Optional<Double>");
        check(!OptionalDouble.empty().equals(java.util.Optional.empty()), "not equal to Optional.empty");
        check(!OptionalDouble.of(1.0).equals(OptionalDouble.of(2.0)), "different values not equal");
        // Double.compare semantics (already partially covered): NaN reflexive, +0/-0 distinct
        check(OptionalDouble.of(Double.NaN).equals(OptionalDouble.of(Double.NaN)), "NaN equals NaN");
        check(!OptionalDouble.of(0.0).equals(OptionalDouble.of(-0.0)), "+0.0 != -0.0");
        check(!OptionalDouble.of(-0.0).equals(OptionalDouble.of(0.0)), "-0.0 != +0.0 symmetric");
        check(OptionalDouble.of(Double.POSITIVE_INFINITY).equals(OptionalDouble.of(Double.POSITIVE_INFINITY)),
              "+Inf equals");
    }

    // toString is exactly "OptionalDouble[<d>]" / "OptionalDouble.empty" (Double.toString form).
    static void testToStringExact() {
        check(OptionalDouble.of(0.0).toString().equals("OptionalDouble[0.0]"), "toString 0.0");
        check(OptionalDouble.of(-0.0).toString().equals("OptionalDouble[-0.0]"), "toString -0.0 keeps sign");
        check(OptionalDouble.of(Double.NaN).toString().equals("OptionalDouble[NaN]"), "toString NaN");
        check(OptionalDouble.of(Double.POSITIVE_INFINITY).toString()
              .equals("OptionalDouble[Infinity]"), "toString +Inf");
        check(OptionalDouble.of(Double.NEGATIVE_INFINITY).toString()
              .equals("OptionalDouble[-Infinity]"), "toString -Inf");
        check(OptionalDouble.of(1.5e300).toString()
              .equals("OptionalDouble[1.5E300]"), "toString scientific form matches Double.toString");
        check(OptionalDouble.empty().toString().equals("OptionalDouble.empty"), "toString empty");
    }

    // orElse with special values passes them through verbatim.
    static void testOrElseSpecialValues() {
        check(Double.isNaN(OptionalDouble.empty().orElse(Double.NaN)), "orElse NaN passthrough");
        check(Double.valueOf(OptionalDouble.empty().orElse(-0.0)).equals(-0.0)
              && 1 / OptionalDouble.empty().orElse(-0.0) == Double.NEGATIVE_INFINITY,
              "orElse -0.0 bit pattern preserved");
        check(OptionalDouble.of(2.5).orElse(9.0) == 2.5, "present ignores orElse");
        check(OptionalDouble.of(2.5).orElse(Double.NaN) == 2.5, "present ignores orElse NaN");
    }

    // Callback exceptions relay the same instance; the container is unaffected.
    static void testCallbackExceptionRelay() {
        final RuntimeException relay = new RuntimeException("relay");
        try {
            OptionalDouble.of(2.5).ifPresent(v -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "ifPresent relays consumer exception unchanged");
        }
        try {
            OptionalDouble.empty().orElseGet(() -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "orElseGet relays supplier exception unchanged");
        }
        OptionalDouble keep = OptionalDouble.of(3.5);
        try { keep.ifPresent(v -> { throw relay; }); } catch (RuntimeException ignored) { }
        check(keep.getAsDouble() == 3.5, "container unchanged after relayed exception");
    }

    // API surface lock: no map/flatMap/ofNullable/isNaN/isInfinite; JDK-9+ additions absent on Java 8.
    static void testSurfaceLock() throws Exception {
        Class<?> c = OptionalDouble.class;
        for (String absent : new String[] {"map", "flatMap", "ofNullable", "isEmpty",
                                           "stream", "isNaN", "isInfinite", "getAsInt"}) {
            try {
                c.getMethod(absent);
                throw new AssertionError(absent + " should not exist");
            } catch (NoSuchMethodException expected) { passed++; }
        }
        try {
            c.getMethod("ifPresentOrElse", java.util.function.DoubleConsumer.class, Runnable.class);
            throw new AssertionError("ifPresentOrElse should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        try {
            c.getMethod("orElseThrow");
            throw new AssertionError("no-arg orElseThrow should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        c.getMethod("of", double.class);
        c.getMethod("getAsDouble");
        c.getMethod("orElseGet", java.util.function.DoubleSupplier.class);
        passed++; // Java 8 surface complete
    }

    public static void main(String[] args) throws Exception {
        testOfGet();
        testBoundaries();
        testOrElse();
        testIfPresent();
        testEqualsHashCodeToString();
        testExceptionMessages();
        testHashCodeFormula();
        testEqualsCrossType();
        testToStringExact();
        testOrElseSpecialValues();
        testCallbackExceptionRelay();
        testSurfaceLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
