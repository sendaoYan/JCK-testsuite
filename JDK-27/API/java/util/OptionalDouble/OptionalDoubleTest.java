/*
 * @test
 * @summary java.util.OptionalDouble conformance: white-box structure (final class / no map/flatMap
 *          / no ofNullable) + black-box behavior (of/empty, isPresent/isEmpty, getAsDouble,
 *          orElse/orElseGet/orElseThrow, ifPresent/ifPresentOrElse, stream, equals).
 * @run main OptionalDoubleTest
 */

import java.lang.reflect.Modifier;
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.OptionalDouble");
        check(!c.isInterface(), "OptionalDouble is not an interface");
        check(Modifier.isPublic(c.getModifiers()), "OptionalDouble is public");
        check(Modifier.isFinal(c.getModifiers()), "OptionalDouble is final");
        check(c.getSuperclass() == Object.class, "OptionalDouble extends Object");
        check(c.getInterfaces().length == 0, "OptionalDouble implements no interfaces");

        check(hasMethod(c, "empty"), "OptionalDouble#empty()");
        check(hasMethod(c, "of", double.class), "OptionalDouble#of(double)");
        check(hasMethod(c, "getAsDouble"), "OptionalDouble#getAsDouble()");
        check(hasMethod(c, "isPresent"), "OptionalDouble#isPresent()");
        check(hasMethod(c, "isEmpty"), "OptionalDouble#isEmpty()");
        check(hasMethod(c, "orElse", double.class), "OptionalDouble#orElse(double)");
        check(hasMethod(c, "orElseThrow"), "OptionalDouble#orElseThrow()");
        check(hasMethod(c, "stream"), "OptionalDouble#stream()");
        check(!hasMethod(c, "ofNullable", Object.class), "OptionalDouble has no ofNullable");
        check(!hasMethod(c, "map"), "OptionalDouble has no map");
        check(!hasMethod(c, "flatMap"), "OptionalDouble has no flatMap");
        check(!hasMethod(c, "filter"), "OptionalDouble has no filter");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testOfGet() {
        OptionalDouble present = OptionalDouble.of(2.5);
        check(present.isPresent() && !present.isEmpty() && present.getAsDouble() == 2.5,
              "of/isPresent/isEmpty/getAsDouble");
        OptionalDouble empty = OptionalDouble.empty();
        check(!empty.isPresent() && empty.isEmpty(), "empty isPresent/isEmpty");
        expectThrows(NoSuchElementException.class, () -> empty.getAsDouble(),
            "empty getAsDouble throws NoSuchElementException");
    }

    static void testOrElse() {
        OptionalDouble present = OptionalDouble.of(2.5);
        OptionalDouble empty = OptionalDouble.empty();

        check(present.orElse(9.0) == 2.5, "present orElse");
        check(empty.orElse(9.0) == 9.0, "empty orElse");

        final AtomicBoolean supplierCalled = new AtomicBoolean(false);
        check(present.orElseGet(() -> { supplierCalled.set(true); return 9.0; }) == 2.5,
              "present orElseGet");
        check(!supplierCalled.get(), "orElseGet supplier not called when present");
        check(empty.orElseGet(() -> 9.0) == 9.0, "empty orElseGet");

        check(present.orElseThrow() == 2.5, "present orElseThrow()");
        expectThrows(NoSuchElementException.class, () -> empty.orElseThrow(),
            "empty orElseThrow() throws NSEE");

        check(present.orElseThrow(() -> new IllegalStateException("nope")) == 2.5,
              "present orElseThrow(supplier)");
        expectThrows(IllegalStateException.class,
            () -> empty.orElseThrow(() -> new IllegalStateException("nope")),
            "empty orElseThrow(supplier) uses supplier");
    }

    static void testIfPresentAndStream() {
        OptionalDouble present = OptionalDouble.of(2.5);
        OptionalDouble empty = OptionalDouble.empty();

        final double[] consumed = {0.0};
        final AtomicBoolean emptyAction = new AtomicBoolean(false);
        present.ifPresent(v -> consumed[0] = v);
        check(consumed[0] == 2.5, "ifPresent invokes DoubleConsumer");
        empty.ifPresent(v -> consumed[0] = v);
        check(consumed[0] == 2.5, "ifPresent on empty does nothing");

        present.ifPresentOrElse(v -> consumed[0] = v, () -> emptyAction.set(true));
        check(consumed[0] == 2.5 && !emptyAction.get(), "ifPresentOrElse present");
        empty.ifPresentOrElse(v -> consumed[0] = v, () -> emptyAction.set(true));
        check(consumed[0] == 2.5 && emptyAction.get(), "ifPresentOrElse empty action");

        check(present.stream().count() == 1 && present.stream().findFirst().getAsDouble() == 2.5,
              "stream on present one element");
        check(empty.stream().count() == 0, "stream on empty zero elements");

        double fallback = java.util.stream.DoubleStream.of(3.0, 1.0, 2.0).min().orElse(-1.0);
        check(fallback == 1.0, "DoubleStream.min() -> OptionalDouble orElse chain");
    }

    static void testEqualsToString() {
        check(OptionalDouble.of(2.5).equals(OptionalDouble.of(2.5)), "equals present");
        check(OptionalDouble.empty().equals(OptionalDouble.empty()), "equals empty");
        check(!OptionalDouble.of(2.5).equals(OptionalDouble.of(3.5)), "!equals different");
        check(!OptionalDouble.of(2.5).equals(OptionalDouble.empty()), "!equals present vs empty");
        check(OptionalDouble.of(2.5).hashCode() == OptionalDouble.of(2.5).hashCode(), "hashCode stable");
        check(OptionalDouble.of(2.5).toString().equals("OptionalDouble[2.5]"), "toString present");
        check(OptionalDouble.empty().toString().equals("OptionalDouble.empty"), "toString empty");
    }

    static void testBoundaryEquivalence() {
        // boundary values.
        check(OptionalDouble.of(0.0).getAsDouble() == 0.0, "of(0.0)");
        check(OptionalDouble.of(-0.0).getAsDouble() == -0.0, "of(-0.0)");
        check(OptionalDouble.of(Double.NaN).getAsDouble() != OptionalDouble.of(Double.NaN).getAsDouble(),
              "getAsDouble NaN != NaN");

        // equals uses Double.compare: NaN equals NaN, +0.0 != -0.0.
        check(OptionalDouble.of(Double.NaN).equals(OptionalDouble.of(Double.NaN)), "equals NaN==NaN");
        check(!OptionalDouble.of(0.0).equals(OptionalDouble.of(-0.0)), "equals +0.0 != -0.0");

        // orElseThrow(supplier) on empty uses the supplied exception message.
        try {
            OptionalDouble.empty().orElseThrow(() -> new IllegalArgumentException("no double"));
            throw new AssertionError("expected IAE");
        } catch (IllegalArgumentException expected) {
            check("no double".equals(expected.getMessage()), "orElseThrow supplier message");
        }

        // equals/hashCode consistency and stream round-trip.
        check(OptionalDouble.of(2.5).hashCode() == OptionalDouble.of(2.5).hashCode(),
              "hashCode stable");
        check(OptionalDouble.of(2.5).stream().findFirst().getAsDouble() == 2.5, "stream round-trip");
    }


    static void testNullMatrix() {
        OptionalDouble present = OptionalDouble.of(2.5);
        OptionalDouble empty = OptionalDouble.empty();

        check(present.orElseGet(null) == 2.5, "orElseGet(null) present returns value");
        expectThrows(NullPointerException.class, () -> empty.orElseGet(null),
            "orElseGet(null) empty NPE");

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

    // ===================== 契约深化（追加） =====================

    // NSEE 消息文本属于可观察契约面。
    static void testExceptionMessages() {
        try {
            OptionalDouble.empty().getAsDouble();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty getAsDouble NSEE message");
        }
        try {
            OptionalDouble.empty().orElseThrow();
            throw new AssertionError("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            check("No value present".equals(e.getMessage()), "empty orElseThrow() NSEE message");
        }
    }

    // hashCode() == (isPresent) ? Double.hashCode(value) : 0；完整位模式公式。
    static void testHashCodeFormula() {
        check(OptionalDouble.empty().hashCode() == 0, "empty hashCode == 0");
        check(OptionalDouble.of(0.0).hashCode() == 0, "of(0.0) hashCode == 0");
        check(OptionalDouble.of(-0.0).hashCode() == Integer.MIN_VALUE,
              "of(-0.0) hashCode == Integer.MIN_VALUE (sign bit in high word)");
        check(OptionalDouble.of(1.0).hashCode() == 1072693248, "of(1.0) hashCode == 0x3FF00000");
        check(OptionalDouble.of(Double.NaN).hashCode() == Double.hashCode(Double.NaN),
              "of(NaN) hashCode == Double.hashCode(NaN)");
        check(OptionalDouble.of(0.0).hashCode() != OptionalDouble.of(-0.0).hashCode(),
              "hashCode distinguishes +0.0 from -0.0");
        check(OptionalDouble.of(2.5).hashCode() == Double.hashCode(2.5),
              "delegates to Double.hashCode");
    }

    // equals 只与 OptionalDouble 比较；Double.compare 语义（NaN 自反、±0 区分）。
    static void testEqualsCrossType() {
        check(!OptionalDouble.of(2.5).equals(null), "equals(null) false");
        check(!OptionalDouble.of(2.5).equals(Double.valueOf(2.5)), "not equal to plain Double");
        check(!OptionalDouble.of(2.5).equals(java.util.Optional.of(2.5)), "not equal to Optional<Double>");
        check(!OptionalDouble.empty().equals(java.util.Optional.empty()), "not equal to Optional.empty");
        check(!OptionalDouble.of(1.0).equals(OptionalDouble.of(2.0)), "different values not equal");
        check(!OptionalDouble.of(-0.0).equals(OptionalDouble.of(0.0)), "-0.0 != +0.0 symmetric");
        check(OptionalDouble.of(Double.POSITIVE_INFINITY).equals(OptionalDouble.of(Double.POSITIVE_INFINITY)),
              "+Inf equals");
    }

    // toString 恒为 "OptionalDouble[<d>]" / "OptionalDouble.empty"（Double.toString 形态）。
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
    }

    // orElse 特殊值按位原样透传。
    static void testOrElseSpecialValues() {
        check(Double.isNaN(OptionalDouble.empty().orElse(Double.NaN)), "orElse NaN passthrough");
        check(Double.valueOf(OptionalDouble.empty().orElse(-0.0)).equals(-0.0)
              && 1 / OptionalDouble.empty().orElse(-0.0) == Double.NEGATIVE_INFINITY,
              "orElse -0.0 bit pattern preserved");
        check(OptionalDouble.of(2.5).orElse(9.0) == 2.5, "present ignores orElse");
    }

    // 回调异常按同一实例透传；容器状态不被破坏。
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

    // JDK-9+ 新方法的 null 矩阵与流语义补全。
    static void testJ9PlusDeep() {
        expectThrows(NullPointerException.class,
            () -> OptionalDouble.of(2.5).ifPresentOrElse(null, () -> { }),
            "ifPresentOrElse(null action, present) NPE");
        expectThrows(NullPointerException.class,
            () -> OptionalDouble.empty().ifPresentOrElse(v -> { }, null),
            "ifPresentOrElse(null emptyAction, empty) NPE");
        OptionalDouble.of(2.5).ifPresentOrElse(v -> { }, null);
        passed++; // null emptyAction with present value: no-op
        OptionalDouble.empty().ifPresentOrElse(null, () -> { });
        passed++; // null action with empty: no-op

        check(OptionalDouble.of(7.5).stream().sum() == 7.5, "stream sum");
        check(OptionalDouble.of(Double.NaN).stream().findFirst().getAsDouble() != 0.0,
              "stream carries NaN verbatim");
        check(OptionalDouble.empty().stream().findAny().isEmpty(), "empty stream no element");
        check(OptionalDouble.of(4.5).orElseThrow() == OptionalDouble.of(4.5).getAsDouble(),
              "orElseThrow() == getAsDouble() on present");
    }

    static void testContractDeepening() throws Exception {
        testExceptionMessages();
        testHashCodeFormula();
        testEqualsCrossType();
        testToStringExact();
        testOrElseSpecialValues();
        testCallbackExceptionRelay();
        testJ9PlusDeep();
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeepening();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
