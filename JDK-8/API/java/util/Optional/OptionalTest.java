/*
 * @test
 * @summary java.util.Optional API conformance (Java 8 surface): complete contract
 *          coverage of factories, get/isPresent, ifPresent, orElse/orElseGet/orElseThrow,
 *          monadic filter/map/flatMap, equals/hashCode/toString, and the full null
 *          semantics matrix (null mapper/predicate/consumer/supplier branches).
 * @run main OptionalTest
 */

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OptionalTest {
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

    // ---------------- factories ----------------
    static void testCreation() {
        Optional<String> empty = Optional.empty();
        check(!empty.isPresent(), "empty isPresent false");
        Optional<String> of = Optional.of("x");
        check(of.isPresent() && of.get().equals("x"), "of/get");
        check(Optional.ofNullable(null).equals(Optional.empty()), "ofNullable(null) empty");
        check(Optional.ofNullable("y").get().equals("y"), "ofNullable(value) present");
        check(Optional.ofNullable("z").equals(Optional.of("z")), "ofNullable(z) == of(z)");

        expectThrows(NullPointerException.class, () -> Optional.of(null), "of(null) NPE");
        // of(null) must throw before any state is observable.
        expectThrows(NullPointerException.class, () -> Optional.of((String) null), "of(null) NPE again");
    }

    // ---------------- get / isPresent ----------------
    static void testGetIsPresent() {
        check(Optional.of("x").get().equals("x"), "get present");
        expectThrows(NoSuchElementException.class, () -> Optional.empty().get(),
            "empty get NoSuchElementException");
        check(Optional.of("x").isPresent() && !Optional.<String>empty().isPresent(),
              "isPresent both states");
    }

    // ---------------- orElse family ----------------
    static void testOrElse() {
        check(Optional.empty().orElse("dflt").equals("dflt"), "orElse on empty");
        check(Optional.of("x").orElse("dflt").equals("x"), "orElse on present");
        // orElse(other) may return null: a missing value is distinguishable from a mapped null
        // only when the container actually holds null (it cannot here); but orElse(null) is legal.
        check(Optional.<String>empty().orElse(null) == null, "orElse(null) on empty returns null");
        check(Optional.of("x").orElse(null).equals("x"), "orElse(null) on present returns value");

        final AtomicBoolean supplierCalled = new AtomicBoolean(false);
        check(Optional.of("x").orElseGet(() -> { supplierCalled.set(true); return "gen"; })
            .equals("x"), "orElseGet on present");
        check(!supplierCalled.get(), "orElseGet supplier not called when present");
        check(Optional.empty().orElseGet(() -> "gen").equals("gen"), "orElseGet on empty");

        // orElseGet(null): present short-circuits (no NPE); empty invokes null -> NPE.
        check(Optional.of("x").orElseGet(null).equals("x"), "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> Optional.empty().orElseGet(null),
            "orElseGet(null) empty NPE");

        check(Optional.of("x").orElseThrow(() -> new IllegalStateException("nope")).equals("x"),
              "orElseThrow(supplier) on present");
        // orElseThrow(null): present short-circuits; empty invokes null -> NPE.
        check(Optional.of("x").orElseThrow(null).equals("x"), "orElseThrow(null) present returns value");
        expectThrows(NullPointerException.class, () -> Optional.empty().orElseThrow(null),
            "orElseThrow(null) empty NPE");

        // supplier returning null on empty -> `throw null` => NPE.
        expectThrows(NullPointerException.class,
            () -> Optional.empty().orElseThrow(() -> (RuntimeException) null),
            "orElseThrow supplier returns null NPE");

        // supplier exception object is passed through unchanged (same instance/message).
        IllegalStateException ex = new IllegalStateException("boom");
        try {
            Optional.empty().orElseThrow(() -> ex);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException caught) {
            check(caught == ex, "orElseThrow passes through same exception object");
        }
    }

    // ---------------- ifPresent ----------------
    static void testIfPresent() {
        final AtomicReference<String> ref = new AtomicReference<>();
        Optional.of("x").ifPresent(ref::set);
        check("x".equals(ref.get()), "ifPresent invokes consumer");
        Optional.<String>empty().ifPresent(v -> ref.set("WRONG"));
        check("x".equals(ref.get()), "ifPresent on empty does nothing");

        // null consumer: present -> NPE; empty -> no-op (documented: NPE only if present).
        expectThrows(NullPointerException.class, () -> Optional.of("x").ifPresent(null),
            "ifPresent(null) present NPE");
        Optional.<String>empty().ifPresent(null);
        passed++; // documented no-op for empty + null consumer
    }

    // ---------------- filter ----------------
    static void testFilter() {
        Optional<String> present = Optional.of("hello");
        check(present.filter(s -> s.length() > 3).get().equals("hello"), "filter keep");
        check(!present.filter(s -> s.length() > 10).isPresent(), "filter drop -> empty");
        check(present.filter(s -> s.length() > 10).equals(Optional.empty()), "filter drop equals empty");

        final boolean[] invoked = {false};
        check(Optional.<String>empty().filter(s -> { invoked[0] = true; return true; })
            .equals(Optional.empty()), "filter empty stays empty");
        check(!invoked[0], "filter predicate not invoked on empty");

        expectThrows(NullPointerException.class, () -> present.filter(null), "filter(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().filter(null),
            "filter(null) empty NPE (null-check first)");
    }

    // ---------------- map ----------------
    static void testMap() {
        Optional<String> present = Optional.of("hello");
        check(present.map(String::length).get() == 5, "map");
        check(Optional.<String>empty().map(String::length).equals(Optional.empty()),
              "map empty stays empty");
        check(present.map(s -> (String) null).equals(Optional.empty()),
              "map null result -> empty");
        check(Optional.of("x").map(s -> "up:" + s).get().equals("up:x"), "map transform");

        expectThrows(NullPointerException.class, () -> present.map(null), "map(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().map(null),
            "map(null) empty NPE (null-check first)");
    }

    // ---------------- flatMap ----------------
    static void testFlatMap() {
        Optional<String> present = Optional.of("hello");
        check(present.flatMap(s -> Optional.of(s + "!")).get().equals("hello!"), "flatMap");
        check(Optional.<String>empty().flatMap(s -> Optional.of(s + "!")).equals(Optional.empty()),
              "flatMap empty stays empty");
        check(present.flatMap(s -> Optional.<String>empty()).equals(Optional.empty()),
              "flatMap result empty");

        expectThrows(NullPointerException.class, () -> present.flatMap(s -> null),
            "flatMap null result NPE");
        expectThrows(NullPointerException.class, () -> present.flatMap(null), "flatMap(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().flatMap(null),
            "flatMap(null) empty NPE (null-check first)");
    }

    // ---------------- equals / hashCode / toString ----------------
    static void testEqualsHashCodeToString() {
        check(Optional.of("x").equals(Optional.of("x")), "equals present");
        check(Optional.empty().equals(Optional.empty()), "equals empty");
        check(!Optional.of("x").equals(Optional.of("y")), "!equals different");
        check(!Optional.of("x").equals(Optional.empty()), "!equals present vs empty");
        check(!Optional.<String>empty().equals(Optional.of("x")), "!equals empty vs present");
        check(!Optional.of("x").equals("x"), "!equals against non-Optional");
        check(Optional.of("x").hashCode() == Optional.of("x").hashCode(), "hashCode stable");
        check(Optional.empty().hashCode() == Optional.empty().hashCode(), "empty hashCode stable");

        check(Optional.of("x").toString().equals("Optional[x]"), "toString present");
        check(Optional.empty().toString().equals("Optional.empty"), "toString empty");
    }

    public static void main(String[] args) {
        testCreation();
        testGetIsPresent();
        testOrElse();
        testIfPresent();
        testFilter();
        testMap();
        testFlatMap();
        testEqualsHashCodeToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
