/*
 * @test
 * @summary java.util.Optional API conformance (Java 8 surface): empty/of/ofNullable,
 *          get/isPresent/ifPresent, orElse/orElseGet/orElseThrow, equals/hashCode,
 *          and the monadic filter/map/flatMap.
 * @run main OptionalTest
 */

import java.util.Optional;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class OptionalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCreation() {
        Optional<String> empty = Optional.empty();
        check(!empty.isPresent(), "empty isPresent false");
        Optional<String> of = Optional.of("x");
        check(of.isPresent() && of.get().equals("x"), "of/get");
        Optional<String> nullable = Optional.ofNullable(null);
        check(!nullable.isPresent(), "ofNullable(null) empty");
        check(Optional.ofNullable("y").get().equals("y"), "ofNullable(value) present");
        try {
            Optional.of(null);
            throw new AssertionError("of(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testGetOrElse() {
        check(Optional.of("x").get().equals("x"), "get");
        try {
            Optional.empty().get();
            throw new AssertionError("empty get: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
        check(Optional.empty().orElse("dflt").equals("dflt"), "orElse on empty");
        check(Optional.of("x").orElse("dflt").equals("x"), "orElse on present");
        check(Optional.empty().orElseGet(() -> "gen").equals("gen"), "orElseGet on empty");
        check(Optional.of("x").orElseGet(() -> "gen").equals("x"), "orElseGet on present");
        check(Optional.of("x").orElseThrow(() -> new IllegalStateException("nope")).equals("x"),
              "orElseThrow on present");
        try {
            Optional.empty().orElseThrow(() -> new IllegalStateException("nope"));
            throw new AssertionError("empty orElseThrow: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().equals("nope"), "orElseThrow supplier used");
        }
    }

    static void testMonadic() {
        Optional<String> present = Optional.of("hello");
        check(present.filter(s -> s.length() > 3).get().equals("hello"), "filter keep");
        check(!present.filter(s -> s.length() > 10).isPresent(), "filter drop");
        check(present.map(String::length).get() == 5, "map");
        check(Optional.<String>empty().map(String::length).equals(Optional.empty()),
              "map empty stays empty");
        check(present.flatMap(s -> Optional.of(s + "!")).get().equals("hello!"), "flatMap");
        check(Optional.<String>empty().flatMap(s -> Optional.of(s + "!")).equals(Optional.empty()),
              "flatMap empty stays empty");
    }

    static void testIfPresentEquals() {
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
    }

    public static void main(String[] args) {
        testCreation();
        testGetOrElse();
        testMonadic();
        testIfPresentEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
