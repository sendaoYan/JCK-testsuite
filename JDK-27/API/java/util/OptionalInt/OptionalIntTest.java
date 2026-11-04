/*
 * @test
 * @summary java.util.OptionalInt conformance: white-box structure (final class / no map/flatMap
 *          / no ofNullable) + black-box behavior (of/empty, isPresent/isEmpty, getAsInt,
 *          orElse/orElseGet/orElseThrow, ifPresent/ifPresentOrElse, stream, equals).
 * @run main OptionalIntTest
 */

import java.lang.reflect.Modifier;
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.OptionalInt");
        check(!c.isInterface(), "OptionalInt is not an interface");
        check(Modifier.isPublic(c.getModifiers()), "OptionalInt is public");
        check(Modifier.isFinal(c.getModifiers()), "OptionalInt is final");
        check(c.getSuperclass() == Object.class, "OptionalInt extends Object");
        check(c.getInterfaces().length == 0, "OptionalInt implements no interfaces");

        check(hasMethod(c, "empty"), "OptionalInt#empty()");
        check(hasMethod(c, "of", int.class), "OptionalInt#of(int)");
        check(hasMethod(c, "getAsInt"), "OptionalInt#getAsInt()");
        check(hasMethod(c, "isPresent"), "OptionalInt#isPresent()");
        check(hasMethod(c, "isEmpty"), "OptionalInt#isEmpty()");
        check(hasMethod(c, "orElse", int.class), "OptionalInt#orElse(int)");
        check(hasMethod(c, "orElseThrow"), "OptionalInt#orElseThrow()");
        check(hasMethod(c, "stream"), "OptionalInt#stream()");
        // 特化 Optional 无 ofNullable / map / flatMap / filter（与 Optional 的重载差异）
        check(!hasMethod(c, "ofNullable", Object.class), "OptionalInt has no ofNullable");
        check(!hasMethod(c, "map"), "OptionalInt has no map");
        check(!hasMethod(c, "flatMap"), "OptionalInt has no flatMap");
        check(!hasMethod(c, "filter"), "OptionalInt has no filter");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testOfGet() {
        OptionalInt present = OptionalInt.of(5);
        check(present.isPresent() && !present.isEmpty() && present.getAsInt() == 5,
              "of/isPresent/isEmpty/getAsInt");
        OptionalInt empty = OptionalInt.empty();
        check(!empty.isPresent() && empty.isEmpty(), "empty isPresent/isEmpty");
        expectThrows(NoSuchElementException.class, () -> empty.getAsInt(),
            "empty getAsInt throws NoSuchElementException");
    }

    static void testOrElse() {
        OptionalInt present = OptionalInt.of(5);
        OptionalInt empty = OptionalInt.empty();

        check(present.orElse(9) == 5, "present orElse");
        check(empty.orElse(9) == 9, "empty orElse");

        final AtomicBoolean supplierCalled = new AtomicBoolean(false);
        check(present.orElseGet(() -> { supplierCalled.set(true); return 9; }) == 5,
              "present orElseGet");
        check(!supplierCalled.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 9) == 9, "empty orElseGet");

        check(present.orElseThrow() == 5, "present orElseThrow()");
        expectThrows(NoSuchElementException.class, () -> empty.orElseThrow(),
            "empty orElseThrow() throws NSEE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 5,
              "present orElseThrow(supplier)");
        expectThrows(IllegalStateException.class,
            () -> empty.orElseThrow(() -> new IllegalStateException("nope")),
            "empty orElseThrow(supplier) uses supplier");
    }

    static void testIfPresentAndStream() {
        OptionalInt present = OptionalInt.of(5);
        OptionalInt empty = OptionalInt.empty();

        final AtomicInteger consumed = new AtomicInteger(0);
        final AtomicBoolean emptyAction = new AtomicBoolean(false);
        present.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 5, "ifPresent invokes IntConsumer");
        empty.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 5, "ifPresent on empty does nothing");

        present.ifPresentOrElse(v -> consumed.set(v), () -> emptyAction.set(true));
        check(consumed.get() == 5 && !emptyAction.get(), "ifPresentOrElse present");
        empty.ifPresentOrElse(v -> consumed.set(v), () -> emptyAction.set(true));
        check(consumed.get() == 5 && emptyAction.get(), "ifPresentOrElse empty action");

        check(present.stream().count() == 1 && present.stream().findFirst().getAsInt() == 5,
              "stream on present one element");
        check(empty.stream().count() == 0, "stream on empty zero elements");

        int fallback = java.util.stream.IntStream.of(3, 1, 2).min().orElse(-1);
        check(fallback == 1, "IntStream.min() -> OptionalInt orElse chain");
    }

    static void testEqualsToString() {
        check(OptionalInt.of(5).equals(OptionalInt.of(5)), "equals present");
        check(OptionalInt.empty().equals(OptionalInt.empty()), "equals empty");
        check(!OptionalInt.of(5).equals(OptionalInt.of(6)), "!equals different");
        check(!OptionalInt.of(5).equals(OptionalInt.empty()), "!equals present vs empty");
        check(OptionalInt.of(5).hashCode() == OptionalInt.of(5).hashCode(), "hashCode stable");
        check(OptionalInt.of(5).toString().equals("OptionalInt[5]"), "toString present");
        check(OptionalInt.empty().toString().equals("OptionalInt.empty"), "toString empty");
    }

    static void testBoundaryEquivalence() {
        // boundary values round-trip.
        check(OptionalInt.of(Integer.MIN_VALUE).getAsInt() == Integer.MIN_VALUE, "of(MIN)");
        check(OptionalInt.of(Integer.MAX_VALUE).getAsInt() == Integer.MAX_VALUE, "of(MAX)");
        check(OptionalInt.of(0).getAsInt() == 0, "of(0)");
        check(OptionalInt.of(-1).getAsInt() == -1, "of(-1)");

        // orElseThrow(supplier) on empty uses the supplied exception message.
        try {
            OptionalInt.empty().orElseThrow(() -> new IllegalArgumentException("no int"));
            throw new AssertionError("expected IAE");
        } catch (IllegalArgumentException expected) {
            check("no int".equals(expected.getMessage()), "orElseThrow supplier message");
        }

        // equals/hashCode consistency.
        check(OptionalInt.of(7).hashCode() == OptionalInt.of(7).hashCode(), "hashCode stable");
        check(OptionalInt.of(7).equals(OptionalInt.of(7)), "equals present");

        // stream round-trip recovers the wrapped value.
        check(OptionalInt.of(7).stream().findFirst().getAsInt() == 7, "stream round-trip");

        // ifPresent on empty does not invoke the consumer.
        final int[] consumed = {-1};
        OptionalInt.empty().ifPresent(v -> consumed[0] = v);
        check(consumed[0] == -1, "ifPresent empty no-op");
    }


    static void testNullMatrix() {
        OptionalInt present = OptionalInt.of(5);
        OptionalInt empty = OptionalInt.empty();

        check(present.orElseGet(null) == 5, "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseGet(null),
            "orElseGet(null) empty NPE");

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

        expectThrows(NullPointerException.class, () -> present.ifPresent(null),
            "ifPresent(null) present NPE");
        empty.ifPresent(null);
        passed++;
    }

    static void testBlackBox() throws Exception {
        testOfGet();
        testOrElse();
        testIfPresentAndStream();
        testEqualsToString();
        testBoundaryEquivalence();
        testNullMatrix();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
