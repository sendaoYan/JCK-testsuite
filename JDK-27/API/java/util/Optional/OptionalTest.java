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


    static void testBlackBox() throws Exception {
        testCreation();
        testGetOrElse();
        testMonadic();
        testOrStreamIfPresentOrElse();
        testIfPresentEqualsToString();
        testBoundaryEquivalence();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
