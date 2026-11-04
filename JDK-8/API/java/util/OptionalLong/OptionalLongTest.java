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

    // ============ contract deepening (appended) ============

    // NSEE message text is observable contract surface.
    static void testExceptionMessages() {
        try {
            OptionalLong.empty().getAsLong();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty getAsLong NSEE message");
        }
    }

    // hashCode() == (isPresent) ? Long.hashCode(value) : 0.
    static void testHashCodeFormula() {
        check(OptionalLong.empty().hashCode() == 0, "empty hashCode == 0");
        check(OptionalLong.of(10L).hashCode() == Long.hashCode(10L), "of(10) hashCode == Long.hashCode(10)");
        check(OptionalLong.of(-1L).hashCode() == Long.hashCode(-1L), "of(-1) hashCode");
        check(OptionalLong.of(Long.MIN_VALUE).hashCode() == Long.hashCode(Long.MIN_VALUE),
              "of(MIN_VALUE) hashCode == Long.hashCode(MIN_VALUE)");
        check(OptionalLong.of(Long.MAX_VALUE).hashCode() == Long.hashCode(Long.MAX_VALUE),
              "of(MAX_VALUE) hashCode == Long.hashCode(MAX_VALUE)");
    }

    // equals only compares against OptionalLong.
    static void testEqualsCrossType() {
        check(!OptionalLong.of(5L).equals(null), "equals(null) false");
        check(!OptionalLong.of(5L).equals(Long.valueOf(5L)), "not equal to plain Long");
        check(!OptionalLong.of(5L).equals(java.util.Optional.of(5L)), "not equal to Optional<Long>");
        check(!OptionalLong.of(5L).equals(java.util.OptionalInt.of(5)), "not equal to OptionalInt");
        check(OptionalLong.of(5L).equals(OptionalLong.of(5L)), "equal values equal");
        check(!OptionalLong.of(5L).equals(OptionalLong.of(6L)), "different values not equal");
        check(!OptionalLong.empty().equals(java.util.Optional.empty()), "not equal to Optional.empty");
    }

    // toString is exactly "OptionalLong[<v>]" / "OptionalLong.empty".
    static void testToStringExact() {
        check(OptionalLong.of(Long.MIN_VALUE).toString()
              .equals("OptionalLong[-9223372036854775808]"), "toString MIN_VALUE");
        check(OptionalLong.of(Long.MAX_VALUE).toString()
              .equals("OptionalLong[9223372036854775807]"), "toString MAX_VALUE");
        check(OptionalLong.of(0L).toString().equals("OptionalLong[0]"), "toString 0");
        check(OptionalLong.of(-1L).toString().equals("OptionalLong[-1]"), "toString negative");
        check(OptionalLong.empty().toString().equals("OptionalLong.empty"), "toString empty");
    }

    // Callback exceptions relay the same instance; the container is unaffected.
    static void testCallbackExceptionRelay() {
        final RuntimeException relay = new RuntimeException("relay");
        try {
            OptionalLong.of(10L).ifPresent(v -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "ifPresent relays consumer exception unchanged");
        }
        try {
            OptionalLong.empty().orElseGet(() -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "orElseGet relays supplier exception unchanged");
        }
        OptionalLong keep = OptionalLong.of(11L);
        try { keep.ifPresent(v -> { throw relay; }); } catch (RuntimeException ignored) { }
        check(keep.getAsLong() == 11L, "container unchanged after relayed exception");
    }

    // API surface lock: no map/flatMap/ofNullable; JDK-9+ additions absent on Java 8.
    static void testSurfaceLock() throws Exception {
        Class<?> c = OptionalLong.class;
        for (String absent : new String[] {"map", "flatMap", "ofNullable", "isEmpty", "stream"}) {
            try {
                c.getMethod(absent);
                throw new AssertionError(absent + " should not exist");
            } catch (NoSuchMethodException expected) { passed++; }
        }
        try {
            c.getMethod("ifPresentOrElse", java.util.function.LongConsumer.class, Runnable.class);
            throw new AssertionError("ifPresentOrElse should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        try {
            c.getMethod("orElseThrow");
            throw new AssertionError("no-arg orElseThrow should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        c.getMethod("of", long.class);
        c.getMethod("getAsLong");
        c.getMethod("orElseGet", java.util.function.LongSupplier.class);
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
        testCallbackExceptionRelay();
        testSurfaceLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
