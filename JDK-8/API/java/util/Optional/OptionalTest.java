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

    // ============ contract deepening (appended) ============

    // NSEE message text is part of the observable contract surface.
    static void testExceptionMessages() {
        try {
            Optional.empty().get();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty get() NSEE message");
        }
    }

    // hashCode() == (value == null) ? 0 : value.hashCode(); empty holds null -> 0.
    static void testHashCodeFormula() {
        check(Optional.empty().hashCode() == 0, "empty hashCode == 0");
        check(Optional.of(1).hashCode() == 1, "of(1) hashCode == 1 (Integer.hashCode)");
        check(Optional.of("a").hashCode() == "a".hashCode(), "of(x) hashCode == x.hashCode()");
        java.util.List<String> val = java.util.Arrays.asList("k");
        check(Optional.of(val).hashCode() == val.hashCode(), "hashCode delegates to value");
        check(Optional.ofNullable(null).hashCode() == 0, "ofNullable(null) hashCode == 0");
        // mutating the held value is reflected (Optional holds a reference, not a snapshot)
        java.util.ArrayList<String> dyn = new java.util.ArrayList<>();
        Optional<java.util.ArrayList<String>> opt = Optional.of(dyn);
        int before = opt.hashCode();
        dyn.add("element");
        check(opt.hashCode() != before, "hashCode tracks live value state");
    }

    // equals compares the held values with equals(), across boxed types it is false.
    static void testEqualsCrossType() {
        check(!Optional.of(1).equals(Optional.of(1L)), "Integer 1 != Long 1");
        check(!Optional.of(1L).equals(Optional.of(1)), "Long 1 != Integer 1 (symmetric)");
        check(!Optional.of(1).equals(Optional.of("1")), "Integer 1 != String \"1\"");
        check(!Optional.of(1).equals(null), "equals(null) false");
        check(Optional.empty().equals(Optional.ofNullable(null)), "empty equals ofNullable(null)");
        check(Optional.ofNullable(null).equals(Optional.empty()), "ofNullable(null) equals empty");
        check(!Optional.of(new int[] {1}).equals(Optional.of(new int[] {1})),
              "array identity: equals on arrays is identity, so Optional of arrays not equal");
        Optional<Number> widened = Optional.<Integer>of(1).map(x -> (Number) x);
        check(widened.equals(Optional.of(1)), "widened Optional<Number> equals Optional<Integer>(1)");
    }

    static void testToStringDeep() {
        Object custom = new Object() {
            @Override public String toString() { return "TOSTR"; }
        };
        check(Optional.of(custom).toString().equals("Optional[TOSTR]"),
              "toString uses held value's toString");
        check(Optional.of("x").toString().equals(Optional.of("x").toString()),
              "toString idempotent");
        check(Optional.empty().toString().equals(Optional.<String>empty().toString()),
              "empty toString stable across generics");
    }

    // Callback exceptions are relayed as the same instance; no wrapping.
    static void testCallbackExceptionRelay() {
        final RuntimeException relay = new RuntimeException("relay");
        try {
            Optional.of("x").filter(s -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "filter relays predicate exception unchanged");
        }
        try {
            Optional.of("x").map(s -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "map relays mapper exception unchanged");
        }
        try {
            Optional.of("x").flatMap(s -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "flatMap relays mapper exception unchanged");
        }
        try {
            Optional.of("x").ifPresent(s -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "ifPresent relays consumer exception unchanged");
        }
        try {
            Optional.empty().orElseGet(() -> { throw relay; });
            throw new AssertionError("expected relay");
        } catch (RuntimeException caught) {
            check(caught == relay, "orElseGet relays supplier exception unchanged");
        }
        // a throwing callback must not corrupt the container
        Optional<String> keep = Optional.of("keep");
        try { keep.map(s -> { throw relay; }); } catch (RuntimeException ignored) { }
        check("keep".equals(keep.get()), "container unchanged after relayed exception");
    }

    // Functor/monad laws on a fixed input.
    static void testMonadicLaws() {
        Optional<String> m = Optional.of("abc");
        java.util.function.Function<String, String> f = s -> s + "1";
        java.util.function.Function<String, String> g = s -> s + "2";
        check(m.map(f).map(g).equals(m.map(f.andThen(g))), "map composition law");
        check(m.map(x -> x).equals(m), "map identity law");
        check(m.flatMap(s -> Optional.of(s).map(f)).equals(m.map(f)), "flatMap/map fusion law");
        check(m.filter(s -> s.length() > 0).filter(s -> s.startsWith("a"))
              .equals(m.filter(s -> s.length() > 0 && s.startsWith("a"))),
              "filter conjunction law");
        check(m.filter(s -> true).equals(m), "filter true law");
        java.util.function.Function<String, Optional<String>> mf = s -> Optional.of(s + "!");
        check(m.flatMap(mf).equals(Optional.of("abc!")), "flatMap flattens");
        // laws also hold on empty
        Optional<String> e = Optional.empty();
        check(e.map(f).map(g).equals(Optional.empty()), "map composition law on empty");
        check(e.filter(s -> true).equals(Optional.empty()), "filter true law on empty");
        check(e.flatMap(mf).equals(Optional.empty()), "flatMap law on empty");
        // two gets on the same instance return equal values (stable content)
        check(m.get().equals(m.get()), "get stable across invocations");
    }

    // get() may be called repeatedly; the value is never consumed.
    static void testRepeatedAccess() {
        Optional<String> opt = Optional.of("v");
        check(opt.get().equals(opt.get()) && opt.get().equals(opt.get()),
              "get is repeatable, value not consumed");
        check(opt.isPresent() && opt.isPresent(), "isPresent repeatable");
    }

    // Java 8 API surface lock: the JDK-9+ additions must be absent here.
    static void testSurfaceLock() throws Exception {
        Class<?> c = Optional.class;
        try { c.getMethod("isEmpty"); throw new AssertionError("isEmpty should not exist on Java 8"); }
        catch (NoSuchMethodException expected) { passed++; }
        try { c.getMethod("ifPresentOrElse", Consumer.class, Runnable.class); throw new AssertionError("ifPresentOrElse should not exist"); }
        catch (NoSuchMethodException expected) { passed++; }
        try { c.getMethod("or", Supplier.class); throw new AssertionError("or should not exist"); }
        catch (NoSuchMethodException expected) { passed++; }
        try { c.getMethod("stream"); throw new AssertionError("stream should not exist"); }
        catch (NoSuchMethodException expected) { passed++; }
        try { c.getMethod("orElseThrow"); throw new AssertionError("no-arg orElseThrow should not exist"); }
        catch (NoSuchMethodException expected) { passed++; }
        // the Java 8 surface itself is fully present
        c.getMethod("get");
        c.getMethod("isPresent");
        c.getMethod("ifPresent", Consumer.class);
        c.getMethod("filter", Predicate.class);
        c.getMethod("map", Function.class);
        c.getMethod("flatMap", Function.class);
        c.getMethod("orElse", Object.class);
        c.getMethod("orElseGet", Supplier.class);
        c.getMethod("orElseThrow", Supplier.class);
        passed++; // Java 8 surface complete
    }

    public static void main(String[] args) throws Exception {
        testCreation();
        testGetIsPresent();
        testOrElse();
        testIfPresent();
        testFilter();
        testMap();
        testFlatMap();
        testEqualsHashCodeToString();
        testExceptionMessages();
        testHashCodeFormula();
        testEqualsCrossType();
        testToStringDeep();
        testCallbackExceptionRelay();
        testMonadicLaws();
        testRepeatedAccess();
        testSurfaceLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
