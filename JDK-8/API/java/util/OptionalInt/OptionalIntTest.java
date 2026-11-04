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

    // ============ contract deepening (appended) ============

    // NSEE message text is observable contract surface.
    static void testExceptionMessages() {
        try {
            OptionalInt.empty().getAsInt();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty getAsInt NSEE message");
        }
    }

    // hashCode() == (isPresent) ? Integer.hashCode(value) : 0.
    static void testHashCodeFormula() {
        check(OptionalInt.empty().hashCode() == 0, "empty hashCode == 0");
        check(OptionalInt.of(5).hashCode() == 5, "of(5) hashCode == 5");
        check(OptionalInt.of(-42).hashCode() == -42, "of(-42) hashCode == -42");
        check(OptionalInt.of(Integer.MIN_VALUE).hashCode() == Integer.MIN_VALUE,
              "of(MIN_VALUE) hashCode == Integer.hashCode(MIN_VALUE)");
        check(OptionalInt.of(Integer.MAX_VALUE).hashCode() == Integer.MAX_VALUE,
              "of(MAX_VALUE) hashCode == Integer.hashCode(MAX_VALUE)");
    }

    // equals only compares against OptionalInt, never against other Optional flavors or boxes.
    static void testEqualsCrossType() {
        check(!OptionalInt.of(5).equals(null), "equals(null) false");
        check(!OptionalInt.of(5).equals(Integer.valueOf(5)), "not equal to plain Integer");
        check(!OptionalInt.of(5).equals(java.util.Optional.of(5)), "not equal to Optional<Integer>");
        check(!OptionalInt.of(5).equals(java.util.OptionalLong.of(5)), "not equal to OptionalLong");
        check(OptionalInt.of(5).equals(OptionalInt.of(5)), "equal values equal");
        check(OptionalInt.of(5).equals(OptionalInt.empty()) == false
              && OptionalInt.empty().equals(OptionalInt.of(5)) == false,
              "empty/present never equal, symmetric");
        check(!OptionalInt.empty().equals(java.util.OptionalDouble.empty()), "empty flavors not equal");
    }

    // toString is exactly "OptionalInt[<v>]" / "OptionalInt.empty".
    static void testToStringExact() {
        check(OptionalInt.of(Integer.MIN_VALUE).toString().equals("OptionalInt[-2147483648]"),
              "toString MIN_VALUE");
        check(OptionalInt.of(Integer.MAX_VALUE).toString().equals("OptionalInt[2147483647]"),
              "toString MAX_VALUE");
        check(OptionalInt.of(0).toString().equals("OptionalInt[0]"), "toString 0");
        check(OptionalInt.of(-1).toString().equals("OptionalInt[-1]"), "toString negative");
        check(OptionalInt.empty().toString().equals("OptionalInt.empty"), "toString empty");
        check(OptionalInt.of(7).toString().equals(OptionalInt.of(7).toString()),
              "toString idempotent");
    }

    // Callback exceptions relay the same instance and leave no state to corrupt.
    static void testCallbackExceptionRelay() {
        final RuntimeException relay = new RuntimeException("relay");
        try {
            OptionalInt.of(5).ifPresent(v -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "ifPresent relays consumer exception unchanged");
        }
        try {
            OptionalInt.empty().orElseGet(() -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "orElseGet relays supplier exception unchanged");
        }
        OptionalInt keep = OptionalInt.of(9);
        try { keep.ifPresent(v -> { throw relay; }); } catch (RuntimeException ignored) { }
        check(keep.getAsInt() == 9, "container unchanged after relayed exception");
    }

    // API surface lock: no map/flatMap/ofNullable (they exist only on Optional<T>),
    // and the JDK-9+ additions must be absent on Java 8.
    static void testSurfaceLock() throws Exception {
        Class<?> c = OptionalInt.class;
        for (String absent : new String[] {"map", "flatMap", "ofNullable"}) {
            try {
                c.getMethod(absent);
                throw new AssertionError(absent + " should not exist on OptionalInt");
            } catch (NoSuchMethodException expected) { passed++; }
        }
        for (String absent : new String[] {"isEmpty", "stream"}) {
            try {
                c.getMethod(absent);
                throw new AssertionError(absent + " should not exist on Java 8");
            } catch (NoSuchMethodException expected) { passed++; }
        }
        try {
            c.getMethod("ifPresentOrElse", java.util.function.IntConsumer.class, Runnable.class);
            throw new AssertionError("ifPresentOrElse should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        try {
            c.getMethod("orElseThrow");
            throw new AssertionError("no-arg orElseThrow should not exist on Java 8");
        } catch (NoSuchMethodException expected) { passed++; }
        // Java 8 surface present
        c.getMethod("of", int.class);
        c.getMethod("empty");
        c.getMethod("getAsInt");
        c.getMethod("isPresent");
        c.getMethod("ifPresent", java.util.function.IntConsumer.class);
        c.getMethod("orElse", int.class);
        c.getMethod("orElseGet", java.util.function.IntSupplier.class);
        c.getMethod("orElseThrow", java.util.function.Supplier.class);
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
