/*
 * @test
 * @summary java.util.Optional conformance: white-box structure (final class / member forms) +
 *          black-box behavior (factories, get/isPresent/isEmpty, orElse/orElseGet/orElseThrow,
 *          monadic filter/map/flatMap, or/stream/ifPresentOrElse).
 * @run main OptionalTest
 */

import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Optional");
        check(!c.isInterface(), "Optional is not an interface");
        check(Modifier.isPublic(c.getModifiers()), "Optional is public");
        check(Modifier.isFinal(c.getModifiers()), "Optional is final");
        check(c.getSuperclass() == Object.class, "Optional extends Object");
        check(c.getInterfaces().length == 0, "Optional implements no interfaces");

        check(hasMethod(c, "empty"), "Optional#empty()");
        check(hasMethod(c, "of", Object.class), "Optional#of(T)");
        check(hasMethod(c, "ofNullable", Object.class), "Optional#ofNullable(T)");
        check(hasMethod(c, "get"), "Optional#get()");
        check(hasMethod(c, "isPresent"), "Optional#isPresent()");
        check(hasMethod(c, "isEmpty"), "Optional#isEmpty()");
        check(hasMethod(c, "map", Function.class), "Optional#map(Function)");
        check(hasMethod(c, "flatMap", Function.class), "Optional#flatMap(Function)");
        check(hasMethod(c, "filter", java.util.function.Predicate.class), "Optional#filter(Predicate)");
        check(hasMethod(c, "orElseThrow"), "Optional#orElseThrow()");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testCreation() {
        Optional<String> empty = Optional.empty();
        check(!empty.isPresent() && empty.isEmpty(), "empty isPresent false / isEmpty true");
        Optional<String> of = Optional.of("x");
        check(of.isPresent() && !of.isEmpty() && of.get().equals("x"), "of/get/isEmpty");
        check(Optional.ofNullable(null).isEmpty(), "ofNullable(null) empty");
        check(Optional.ofNullable("y").get().equals("y"), "ofNullable(value) present");
        expectThrows(NullPointerException.class, () -> Optional.of(null), "of(null) throws NPE");
    }

    static void testGetOrElse() {
        check(Optional.of("x").get().equals("x"), "get");
        expectThrows(NoSuchElementException.class, () -> Optional.empty().get(),
            "empty get throws NoSuchElementException");

        check(Optional.empty().orElse("dflt").equals("dflt"), "orElse on empty");
        check(Optional.of("x").orElse("dflt").equals("x"), "orElse on present");

        final AtomicBoolean supplierCalled = new AtomicBoolean(false);
        check(Optional.of("x").orElseGet(() -> { supplierCalled.set(true); return "gen"; })
            .equals("x"), "orElseGet on present");
        check(!supplierCalled.get(), "orElseGet supplier not called when present");
        check(Optional.empty().orElseGet(() -> "gen").equals("gen"), "orElseGet on empty");

        check(Optional.of("x").orElseThrow().equals("x"), "orElseThrow() on present");
        expectThrows(NoSuchElementException.class, () -> Optional.empty().orElseThrow(),
            "orElseThrow() on empty throws NSEE");

        check(Optional.of("x").orElseThrow(() -> new IllegalStateException("nope")).equals("x"),
              "orElseThrow(supplier) on present");
        try {
            Optional.empty().orElseThrow(() -> new IllegalStateException("nope"));
            throw new AssertionError("empty orElseThrow(supplier): expected IllegalStateException");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().equals("nope"), "orElseThrow(supplier) supplier used");
        }
    }

    static void testMonadic() {
        Optional<String> present = Optional.of("hello");
        check(present.filter(s -> s.length() > 3).get().equals("hello"), "filter keep");
        check(present.filter(s -> s.length() > 10).isEmpty(), "filter drop");
        check(present.map(String::length).get() == 5, "map");
        check(Optional.<String>empty().map(String::length).equals(Optional.empty()),
              "map empty stays empty");
        check(present.flatMap(s -> Optional.of(s + "!")).get().equals("hello!"), "flatMap");
        check(Optional.<String>empty().flatMap(s -> Optional.of(s + "!")).equals(Optional.empty()),
              "flatMap empty stays empty");

        String name = Optional.ofNullable("  alice  ").map(String::trim)
            .filter(s -> !s.isEmpty()).orElse("default");
        check(name.equals("alice"), "ofNullable/map/filter/orElse chain");
        check(Optional.ofNullable((String) null).map(String::trim).filter(s -> !s.isEmpty())
            .orElse("default").equals("default"), "empty chain falls back to default");
    }

    static void testOrStreamIfPresentOrElse() {
        Optional<String> present = Optional.of("x");
        Optional<String> empty = Optional.empty();

        check(present.or(() -> Optional.of("fallback")).get().equals("x"), "or on present returns this");
        check(empty.or(() -> Optional.of("fallback")).get().equals("fallback"), "or on empty uses supplier");

        final AtomicBoolean orSupplierCalled = new AtomicBoolean(false);
        present.or(() -> { orSupplierCalled.set(true); return Optional.of("z"); });
        check(!orSupplierCalled.get(), "or supplier not called when present");
        expectThrows(NullPointerException.class, () -> empty.or(null), "or(null) throws NPE");

        check(present.stream().count() == 1 && present.stream().findFirst().get().equals("x"),
              "stream on present has one element");
        check(empty.stream().count() == 0, "stream on empty has zero elements");

        final AtomicReference<String> consumed = new AtomicReference<>();
        final AtomicBoolean emptyAction = new AtomicBoolean(false);
        present.ifPresentOrElse(consumed::set, () -> emptyAction.set(true));
        check(consumed.get().equals("x") && !emptyAction.get(), "ifPresentOrElse present");
        empty.ifPresentOrElse(consumed::set, () -> emptyAction.set(true));
        check(consumed.get().equals("x") && emptyAction.get(), "ifPresentOrElse empty action");
    }

    static void testIfPresentEqualsToString() {
        final AtomicReference<String> ref = new AtomicReference<>();
        Optional.of("x").ifPresent(ref::set);
        check(ref.get().equals("x"), "ifPresent invokes consumer");
        Optional.<String>empty().ifPresent(ref::set);
        check(ref.get().equals("x"), "ifPresent on empty does nothing");

        check(Optional.of("x").equals(Optional.of("x")), "equals present");
        check(Optional.empty().equals(Optional.empty()), "equals empty");
        check(!Optional.of("x").equals(Optional.of("y")), "!equals different");
        check(!Optional.of("x").equals(Optional.empty()), "!equals present vs empty");
        check(Optional.of("x").hashCode() == Optional.of("x").hashCode(), "hashCode stable");

        check(Optional.of("x").toString().equals("Optional[x]"), "toString present");
        check(Optional.empty().toString().equals("Optional.empty"), "toString empty");
    }

    static void testBoundaryEquivalence() {
        // map with null mapper result yields empty; flatMap with null result throws NPE.
        check(Optional.of("x").map(s -> null).equals(Optional.empty()), "map null -> empty");
        expectThrows(NullPointerException.class, () -> Optional.of("x").flatMap(s -> null),
            "flatMap null result throws NPE");

        // orElseThrow(supplier) on empty uses the supplied exception (message stability).
        try {
            Optional.empty().orElseThrow(() -> new IllegalArgumentException("no value"));
            throw new AssertionError("expected IAE");
        } catch (IllegalArgumentException expected) {
            check("no value".equals(expected.getMessage()), "orElseThrow supplier message");
        }

        // or(null) throws NPE.
        expectThrows(NullPointerException.class, () -> Optional.of("x").or(null), "or(null) NPE");

        // equals/hashCode consistency.
        check(Optional.of("x").hashCode() == Optional.of("x").hashCode(), "hashCode stable");
        check(Optional.of("x").equals(Optional.of("x"))
                == (Optional.of("x").hashCode() == Optional.of("x").hashCode()),
              "equals->hashCode consistent");

        // filter on empty stays empty and does not invoke the predicate.
        final boolean[] invoked = {false};
        check(Optional.<String>empty().filter(s -> { invoked[0] = true; return true; }).isEmpty(),
              "filter empty stays empty");
        check(!invoked[0], "filter predicate not invoked on empty");

        // ofNullable(value) == of(value) for non-null.
        check(Optional.ofNullable("z").equals(Optional.of("z")), "ofNullable(z) == of(z)");
    }


    static void testNullMatrix() {
        // orElse(null) is legal and distinguishes missing from mapped-null only via container.
        check(Optional.<String>empty().orElse(null) == null, "orElse(null) empty returns null");
        check(Optional.of("x").orElse(null).equals("x"), "orElse(null) present returns value");

        // orElseGet(null): present short-circuits (no NPE); empty invokes null -> NPE.
        check(Optional.of("x").orElseGet(null).equals("x"), "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> Optional.empty().orElseGet(null),
            "orElseGet(null) empty NPE");

        // orElseThrow(null): present short-circuits; empty invokes null -> NPE.
        check(Optional.of("x").orElseThrow(null).equals("x"), "orElseThrow(null) present returns value");
        expectThrows(NullPointerException.class, () -> Optional.empty().orElseThrow(null),
            "orElseThrow(null) empty NPE");
        expectThrows(NullPointerException.class, () -> Optional.empty().orElseThrow(() -> null),
            "orElseThrow supplier returns null NPE");

        // supplier exception object is passed through unchanged.
        IllegalStateException ex = new IllegalStateException("boom");
        try {
            Optional.empty().orElseThrow(() -> ex);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException caught) {
            check(caught == ex, "orElseThrow passes through same exception object");
        }

        // ifPresent(null): present -> NPE; empty -> no-op (documented).
        expectThrows(NullPointerException.class, () -> Optional.of("x").ifPresent(null),
            "ifPresent(null) present NPE");
        Optional.<String>empty().ifPresent(null);
        passed++;

        // filter/map/flatMap null-check their function even when empty.
        expectThrows(NullPointerException.class, () -> Optional.of("x").filter(null),
            "filter(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().filter(null),
            "filter(null) empty NPE");
        expectThrows(NullPointerException.class, () -> Optional.of("x").map(null),
            "map(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().map(null),
            "map(null) empty NPE");
        expectThrows(NullPointerException.class, () -> Optional.of("x").flatMap(null),
            "flatMap(null) present NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().flatMap(null),
            "flatMap(null) empty NPE");
    }

    static void testBlackBox() throws Exception {
        testCreation();
        testGetOrElse();
        testMonadic();
        testOrStreamIfPresentOrElse();
        testIfPresentEqualsToString();
        testBoundaryEquivalence();
        testNullMatrix();
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
        try {
            Optional.empty().orElseThrow();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty orElseThrow() NSEE message");
        }
    }

    // hashCode() == (value == null) ? 0 : value.hashCode(); empty holds null -> 0.
    static void testHashCodeFormula() {
        check(Optional.empty().hashCode() == 0, "empty hashCode == 0");
        check(Optional.of(1).hashCode() == 1, "of(1) hashCode == 1 (Integer.hashCode)");
        check(Optional.of("a").hashCode() == "a".hashCode(), "of(x) hashCode == x.hashCode()");
        check(Optional.ofNullable(null).hashCode() == 0, "ofNullable(null) hashCode == 0");
        java.util.List<String> val = java.util.Arrays.asList("k");
        check(Optional.of(val).hashCode() == val.hashCode(), "hashCode delegates to value");
        java.util.ArrayList<String> dyn = new java.util.ArrayList<>();
        Optional<java.util.ArrayList<String>> opt = Optional.of(dyn);
        int before = opt.hashCode();
        dyn.add("element");
        check(opt.hashCode() != before, "hashCode tracks live value state");
    }

    // equals compares held values with equals(); boxed types with equal values differ.
    static void testEqualsCrossType() {
        check(!Optional.of(1).equals(Optional.of(1L)), "Integer 1 != Long 1");
        check(!Optional.of(1L).equals(Optional.of(1)), "Long 1 != Integer 1 (symmetric)");
        check(!Optional.of(1).equals(Optional.of("1")), "Integer 1 != String \"1\"");
        check(!Optional.of(1).equals(null), "equals(null) false");
        check(Optional.empty().equals(Optional.ofNullable(null)), "empty equals ofNullable(null)");
        check(Optional.ofNullable(null).equals(Optional.empty()), "ofNullable(null) equals empty");
        check(!Optional.of(new int[] {1}).equals(Optional.of(new int[] {1})),
              "array identity: Optional of arrays not equal");
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
    }

    // Callback exceptions are relayed as the same instance; no wrapping, no state change.
    static void testCallbackExceptionRelay() {
        final RuntimeException relay = new RuntimeException("relay");
        try { Optional.of("x").filter(s -> { throw relay; }); }
        catch (RuntimeException caught) { check(caught == relay, "filter relays predicate exception unchanged"); }
        try { Optional.of("x").map(s -> { throw relay; }); }
        catch (RuntimeException caught) { check(caught == relay, "map relays mapper exception unchanged"); }
        try { Optional.of("x").flatMap(s -> { throw relay; }); }
        catch (RuntimeException caught) { check(caught == relay, "flatMap relays mapper exception unchanged"); }
        try { Optional.of("x").ifPresent(s -> { throw relay; }); }
        catch (RuntimeException caught) { check(caught == relay, "ifPresent relays consumer exception unchanged"); }
        try { Optional.empty().orElseGet(() -> { throw relay; }); }
        catch (RuntimeException caught) { check(caught == relay, "orElseGet relays supplier exception unchanged"); }
        Optional<String> keep = Optional.of("keep");
        try { keep.map(s -> { throw relay; }); } catch (RuntimeException ignored) { }
        check("keep".equals(keep.get()), "container unchanged after relayed exception");
    }

    // Functor/monad laws on fixed inputs.
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
        Optional<String> e = Optional.empty();
        check(e.map(f).map(g).equals(Optional.empty()), "map composition law on empty");
        check(e.filter(s -> true).equals(Optional.empty()), "filter true law on empty");
        check(m.get().equals(m.get()), "get stable across invocations");
    }

    // JDK-9+ method contracts beyond the surface checks above.
    static void testJ9PlusDeep() {
        // or(): supplier result null -> NPE; supplier not consulted when present
        expectThrows(NullPointerException.class, () -> Optional.empty().or(() -> null),
            "or supplier returns null NPE");
        expectThrows(NullPointerException.class, () -> Optional.empty().or(null),
            "or(null) empty NPE");
        final AtomicBoolean called = new AtomicBoolean(false);
        Optional.of("x").or(() -> { called.set(true); return Optional.of("z"); });
        check(!called.get(), "or supplier not invoked when present");
        check(Optional.<String>empty().or(() -> Optional.empty()).isEmpty(),
              "or returning empty keeps it empty");

        // ifPresentOrElse(null, ...) null matrix: NPE only on the branch actually taken
        expectThrows(NullPointerException.class, () -> Optional.of("x").ifPresentOrElse(null, () -> { }),
            "ifPresentOrElse(null action, present) NPE");
        expectThrows(NullPointerException.class, () -> Optional.<String>empty().ifPresentOrElse(v -> { }, null),
            "ifPresentOrElse(null emptyAction, empty) NPE");
        Optional.of("x").ifPresentOrElse(v -> { }, null);
        passed++; // null emptyAction with present value: documented no-op
        Optional.<String>empty().ifPresentOrElse(null, () -> { });
        passed++; // null action with empty: documented no-op

        // stream(): exactly the held value as a one-element Stream
        check(Optional.of("s").stream().toArray()[0].equals("s"), "stream yields held value");
        check(java.util.Arrays.equals(
                    java.util.stream.Stream.of("x").toArray(),
                    Optional.of("x").stream().toArray()),
              "stream content equals Stream.of(value)");
        check(Optional.<String>empty().stream().findAny().isEmpty(), "empty stream no element");
        check(Optional.of(2).stream().mapToInt(Integer::intValue).sum() == 2,
              "stream pipelines further");

        // orElseThrow() vs get(): same NSEE on empty, same value on present
        check(Optional.of("v").orElseThrow().equals(Optional.of("v").get()),
              "orElseThrow() == get() on present");
    }

    static void testContractDeepening() throws Exception {
        testExceptionMessages();
        testHashCodeFormula();
        testEqualsCrossType();
        testToStringDeep();
        testCallbackExceptionRelay();
        testMonadicLaws();
        testJ9PlusDeep();
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeepening();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
