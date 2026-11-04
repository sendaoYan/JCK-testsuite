/*
 * @test
 * @summary java.util.Objects conformance: white-box structure plus black-box behavior of
 *          requireNonNull (message/supplier variants), equals/deepEquals, hashCode/hash,
 *          toString/toIdentityString, compare, isNull/nonNull, requireNonNullElse/ElseGet,
 *          and checkIndex/checkFromToIndex/checkFromIndexSize (int and long).
 * @run main ObjectsTest
 */

import java.util.Objects;

public class ObjectsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Objects");
        check(!c.isInterface(), "Objects is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Objects is final");
        c.getMethod("requireNonNull", Object.class);
        c.getMethod("equals", Object.class, Object.class);
        c.getMethod("deepEquals", Object.class, Object.class);
        c.getMethod("hash", Object[].class);
        c.getMethod("toString", Object.class);
        c.getMethod("compare", Object.class, Object.class, java.util.Comparator.class);
        c.getMethod("requireNonNullElse", Object.class, Object.class);
        c.getMethod("checkIndex", int.class, int.class);
        c.getMethod("checkFromToIndex", int.class, int.class, int.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testRequireNonNull();
        testEquals();
        testDeepEquals();
        testHashCode();
        testToString();
        testCompare();
        testIsNull();
        testRequireNonNullElse();
        testIndexChecks();
        testBoundaryEquivalence();
        testDeepNullSemantics();
    }

    static void testDeepNullSemantics() {
        // requireNonNull(obj, supplier): supplier not called when non-null; null supplier NPE;
        // supplier returning null message -> NPE with null message.
        final boolean[] called = {false};
        Object o = new Object();
        check(Objects.requireNonNull(o, () -> { called[0] = true; return "x"; }) == o,
              "requireNonNull(supplier) non-null");
        check(!called[0], "supplier not called when non-null");
        assertThrows(NullPointerException.class,
                () -> Objects.requireNonNull(null, (java.util.function.Supplier<String>) null),
                "requireNonNull(null, null supplier) NPE");
        try {
            Objects.requireNonNull(null, () -> null);
            throw new AssertionError("expected NPE");
        } catch (NullPointerException e) {
            check(e.getMessage() == null, "supplier null message -> null message");
        }

        // compare null matrix.
        check(Objects.compare(null, null, String::compareTo) == 0, "compare(null,null) == 0");
        check(Objects.compare("a", "a", null) == 0, "compare(a,a,null) == 0 short-circuit");
        assertThrows(NullPointerException.class,
                () -> Objects.compare(null, "x", String::compareTo), "compare(null,x) NPE");
        assertThrows(NullPointerException.class,
                () -> Objects.compare("x", null, String::compareTo), "compare(x,null) NPE");

        // deepEquals nested/2D/array-vs-scalar/int[]-vs-Integer[].
        check(Objects.deepEquals(new int[][]{{1, 2}, {3, 4}}, new int[][]{{1, 2}, {3, 4}}),
              "deepEquals 2D int arrays");
        check(!Objects.deepEquals(new int[]{1}, 1), "deepEquals array vs scalar false");
        check(!Objects.deepEquals(new int[]{1, 2}, new Integer[]{1, 2}),
              "deepEquals int[] vs Integer[] false");

        // hash with null elements and single null element == Arrays.hashCode.
        check(Objects.hash("k", null) == java.util.Arrays.hashCode(new Object[]{"k", null}),
              "hash null element == Arrays.hashCode");
        check(Objects.hash((Object) null) == java.util.Arrays.hashCode(new Object[]{null}),
              "hash single null == Arrays.hashCode");

        // toString of object whose toString() returns null.
        Object nullToString = new Object() { public String toString() { return null; } };
        check(Objects.toString(nullToString) == null, "toString(object null toString) == null");
    }

    static void testRequireNonNull() {
        Object o = new Object();
        check(Objects.requireNonNull(o) == o, "requireNonNull returns object");
        check(Objects.requireNonNull(o, "msg") == o, "requireNonNull(msg) returns object");
        assertThrows(NullPointerException.class,
                () -> Objects.requireNonNull(null), "requireNonNull(null) throws NPE");
        try {
            Objects.requireNonNull(null, "boom");
            throw new AssertionError("expected NPE");
        } catch (NullPointerException e) {
            check("boom".equals(e.getMessage()), "requireNonNull message carried");
        }
        assertThrows(NullPointerException.class,
                () -> Objects.requireNonNull(null, () -> "supplied"),
                "requireNonNull(null, supplier) throws NPE");
    }

    static void testEquals() {
        check(Objects.equals(null, null), "equals(null,null) == true");
        check(!Objects.equals(null, "x"), "equals(null,x) == false");
        check(!Objects.equals("x", null), "equals(x,null) == false");
        check(Objects.equals("x", "x"), "equals(x,x) == true");
        check(!Objects.equals("x", "y"), "equals(x,y) == false");
    }

    static void testDeepEquals() {
        check(Objects.deepEquals(null, null), "deepEquals(null,null) == true");
        check(!Objects.deepEquals(null, new int[]{1}), "deepEquals(null,arr) == false");
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        check(Objects.deepEquals(a, b), "deepEquals arrays element-wise");
        check(!Objects.deepEquals(a, new int[]{1, 2}), "deepEquals different length");
        Object[] oa = {"x", "y"};
        Object[] ob = {"x", "y"};
        check(Objects.deepEquals(oa, ob), "deepEquals object arrays");
    }

    static void testHashCode() {
        check(Objects.hashCode(null) == 0, "hashCode(null) == 0");
        check(Objects.hashCode("x") == "x".hashCode(), "hashCode delegates");
        check(Objects.hash("a", "b") == Objects.hash("a", "b"), "hash varargs stable");
        check(Objects.hash() == 1, "hash() == 1");
    }

    static void testToString() {
        check("null".equals(Objects.toString(null)), "toString(null) == \"null\"");
        check("x".equals(Objects.toString("x")), "toString delegates");
        check("dflt".equals(Objects.toString(null, "dflt")), "toString(null, dflt)");
        check("x".equals(Objects.toString("x", "dflt")), "toString(non-null, dflt)");
        check(Objects.toIdentityString("x").startsWith("java.lang.String@"),
              "toIdentityString includes class name");
    }

    static void testCompare() {
        check(Objects.compare("a", "b", String::compareTo) < 0, "compare < 0");
        check(Objects.compare("b", "a", String::compareTo) > 0, "compare > 0");
        check(Objects.compare("a", "a", String::compareTo) == 0, "compare == 0");
    }

    static void testIsNull() {
        check(Objects.isNull(null), "isNull(null)");
        check(!Objects.isNull("x"), "!isNull(x)");
        check(Objects.nonNull("x"), "nonNull(x)");
        check(!Objects.nonNull(null), "!nonNull(null)");
    }

    static void testRequireNonNullElse() {
        check("x".equals(Objects.requireNonNullElse("x", "dflt")), "requireNonNullElse non-null");
        check("dflt".equals(Objects.requireNonNullElse(null, "dflt")), "requireNonNullElse null");
        check("got".equals(Objects.requireNonNullElseGet(null, () -> "got")),
              "requireNonNullElseGet null supplier");
        check("x".equals(Objects.requireNonNullElseGet("x", () -> "got")),
              "requireNonNullElseGet non-null (supplier not called)");
    }

    static void testIndexChecks() {
        check(Objects.checkIndex(0, 5) == 0, "checkIndex in range");
        check(Objects.checkIndex(4, 5) == 4, "checkIndex upper bound");
        assertThrows(IndexOutOfBoundsException.class,
                () -> Objects.checkIndex(5, 5), "checkIndex == length throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> Objects.checkIndex(-1, 5), "checkIndex negative throws");

        check(Objects.checkFromToIndex(1, 4, 5) == 1, "checkFromToIndex in range");
        check(Objects.checkFromIndexSize(1, 3, 5) == 1, "checkFromIndexSize in range");
        assertThrows(IndexOutOfBoundsException.class,
                () -> Objects.checkFromToIndex(3, 2, 5), "checkFromToIndex from>to throws");

        check(Objects.checkIndex(2L, 5L) == 2L, "checkIndex(long) in range");
        assertThrows(IndexOutOfBoundsException.class,
                () -> Objects.checkIndex(5L, 5L), "checkIndex(long) == length throws");
    }

    static void testBoundaryEquivalence() {
        // checkIndex boundaries.
        check(Objects.checkIndex(0, 1) == 0, "checkIndex(0,1)");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkIndex(0, 0),
                "checkIndex(0,0) throws");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkIndex(-1, 0),
                "checkIndex(-1,0) throws");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkIndex(1, 1),
                "checkIndex(1,1) throws");

        // checkFromToIndex: empty range is valid at any position <= length.
        check(Objects.checkFromToIndex(0, 0, 0) == 0, "checkFromToIndex(0,0,0)");
        check(Objects.checkFromToIndex(5, 5, 5) == 5, "checkFromToIndex(5,5,5)");
        check(Objects.checkFromToIndex(0, 5, 5) == 0, "checkFromToIndex(0,5,5)");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkFromToIndex(5, 0, 5),
                "checkFromToIndex(5,0,5) from>to throws");

        // checkFromIndexSize: size 0 valid at length; negative/overflow size invalid.
        check(Objects.checkFromIndexSize(5, 0, 5) == 5, "checkFromIndexSize(5,0,5)");
        check(Objects.checkFromIndexSize(0, 5, 5) == 0, "checkFromIndexSize(0,5,5)");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkFromIndexSize(0, 6, 5),
                "checkFromIndexSize(0,6,5) overflow throws");
        assertThrows(IndexOutOfBoundsException.class, () -> Objects.checkFromIndexSize(0, -1, 5),
                "checkFromIndexSize(0,-1,5) negative size throws");

        // requireNonNullElseGet supplier not called for non-null.
        final boolean[] called = {false};
        check("x".equals(Objects.requireNonNullElseGet("x", () -> { called[0] = true; return "y"; })),
                "requireNonNullElseGet non-null");
        check(!called[0], "supplier not called when non-null");

        // hash(single) matches Arrays.hashCode of the equivalent varargs array.
        check(Objects.hash("k") == java.util.Arrays.hashCode(new Object[]{"k"}),
              "hash == Arrays.hashCode(varargs)");
        check(Objects.hash((Object[]) null) == 0, "hash(null array) == 0");

        // compare antisymmetry.
        check(Objects.compare("a", "b", String::compareTo)
                == -Objects.compare("b", "a", String::compareTo), "compare antisymmetry");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
