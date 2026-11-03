/*
 * @test
 * @summary java.util.OptionalLong conformance: white-box structure (final class / no map/flatMap
 *          / no ofNullable) + black-box behavior (of/empty, isPresent/isEmpty, getAsLong,
 *          orElse/orElseGet/orElseThrow, ifPresent/ifPresentOrElse, stream, equals).
 * @run main OptionalLongTest
 */

import java.lang.reflect.Modifier;
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.OptionalLong");
        check(!c.isInterface(), "OptionalLong is not an interface");
        check(Modifier.isPublic(c.getModifiers()), "OptionalLong is public");
        check(Modifier.isFinal(c.getModifiers()), "OptionalLong is final");
        check(c.getSuperclass() == Object.class, "OptionalLong extends Object");
        check(c.getInterfaces().length == 0, "OptionalLong implements no interfaces");

        check(hasMethod(c, "empty"), "OptionalLong#empty()");
        check(hasMethod(c, "of", long.class), "OptionalLong#of(long)");
        check(hasMethod(c, "getAsLong"), "OptionalLong#getAsLong()");
        check(hasMethod(c, "isPresent"), "OptionalLong#isPresent()");
        check(hasMethod(c, "isEmpty"), "OptionalLong#isEmpty()");
        check(hasMethod(c, "orElse", long.class), "OptionalLong#orElse(long)");
        check(hasMethod(c, "orElseThrow"), "OptionalLong#orElseThrow()");
        check(hasMethod(c, "stream"), "OptionalLong#stream()");
        check(!hasMethod(c, "ofNullable", Object.class), "OptionalLong has no ofNullable");
        check(!hasMethod(c, "map"), "OptionalLong has no map");
        check(!hasMethod(c, "flatMap"), "OptionalLong has no flatMap");
        check(!hasMethod(c, "filter"), "OptionalLong has no filter");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testOfGet() {
        OptionalLong present = OptionalLong.of(10L);
        check(present.isPresent() && !present.isEmpty() && present.getAsLong() == 10L,
              "of/isPresent/isEmpty/getAsLong");
        OptionalLong empty = OptionalLong.empty();
        check(!empty.isPresent() && empty.isEmpty(), "empty isPresent/isEmpty");
        expectThrows(NoSuchElementException.class, () -> empty.getAsLong(),
            "empty getAsLong throws NoSuchElementException");
    }

    static void testOrElse() {
        OptionalLong present = OptionalLong.of(10L);
        OptionalLong empty = OptionalLong.empty();

        check(present.orElse(99L) == 10L, "present orElse");
        check(empty.orElse(99L) == 99L, "empty orElse");

        final AtomicBoolean supplierCalled = new AtomicBoolean(false);
        check(present.orElseGet(() -> { supplierCalled.set(true); return 99L; }) == 10L,
              "present orElseGet");
        check(!supplierCalled.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 99L) == 99L, "empty orElseGet");

        check(present.orElseThrow() == 10L, "present orElseThrow()");
        expectThrows(NoSuchElementException.class, () -> empty.orElseThrow(),
            "empty orElseThrow() throws NSEE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 10L,
              "present orElseThrow(supplier)");
        expectThrows(IllegalStateException.class,
            () -> empty.orElseThrow(() -> new IllegalStateException("nope")),
            "empty orElseThrow(supplier) uses supplier");
    }

    static void testIfPresentAndStream() {
        OptionalLong present = OptionalLong.of(10L);
        OptionalLong empty = OptionalLong.empty();

        final AtomicLong consumed = new AtomicLong(0L);
        final AtomicBoolean emptyAction = new AtomicBoolean(false);
        present.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 10L, "ifPresent invokes LongConsumer");
        empty.ifPresent(v -> consumed.set(v));
        check(consumed.get() == 10L, "ifPresent on empty does nothing");

        present.ifPresentOrElse(v -> consumed.set(v), () -> emptyAction.set(true));
        check(consumed.get() == 10L && !emptyAction.get(), "ifPresentOrElse present");
        empty.ifPresentOrElse(v -> consumed.set(v), () -> emptyAction.set(true));
        check(consumed.get() == 10L && emptyAction.get(), "ifPresentOrElse empty action");

        check(present.stream().count() == 1 && present.stream().findFirst().getAsLong() == 10L,
              "stream on present one element");
        check(empty.stream().count() == 0, "stream on empty zero elements");

        long fallback = java.util.stream.LongStream.of(3L, 1L, 2L).min().orElse(-1L);
        check(fallback == 1L, "LongStream.min() -> OptionalLong orElse chain");
    }

    static void testEqualsToString() {
        check(OptionalLong.of(10L).equals(OptionalLong.of(10L)), "equals present");
        check(OptionalLong.empty().equals(OptionalLong.empty()), "equals empty");
        check(!OptionalLong.of(10L).equals(OptionalLong.of(11L)), "!equals different");
        check(!OptionalLong.of(10L).equals(OptionalLong.empty()), "!equals present vs empty");
        check(OptionalLong.of(10L).hashCode() == OptionalLong.of(10L).hashCode(), "hashCode stable");
        check(OptionalLong.of(10L).toString().equals("OptionalLong[10]"), "toString present");
        check(OptionalLong.empty().toString().equals("OptionalLong.empty"), "toString empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOfGet();
        testOrElse();
        testIfPresentAndStream();
        testEqualsToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
