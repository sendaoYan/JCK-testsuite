/*
 * @test
 * @summary java.util.Objects API conformance (Java 8 surface): complete contract coverage
 *          of requireNonNull (String + Supplier variants, null-supplier/supplier-null-message),
 *          equals, deepEquals (nested/primitive/null-element/array-vs-non-array), hashCode/hash
 *          (null elements / null varargs array), toString (toString()-returns-null edge),
 *          compare (null matrix + null comparator), isNull/nonNull.
 * @run main ObjectsTest
 */

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class ObjectsTest {
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

    static void testRequireNonNull() {
        Object o = new Object();
        check(Objects.requireNonNull(o) == o, "requireNonNull returns object");
        check(Objects.requireNonNull(o, "msg") == o, "requireNonNull(msg) returns object");
        try {
            Objects.requireNonNull(null);
            throw new AssertionError("requireNonNull(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(expected.getMessage() == null, "requireNonNull(null) message null");
        }
        try {
            Objects.requireNonNull(null, "boom");
            throw new AssertionError("requireNonNull(null,msg): expected NullPointerException");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("boom"), "requireNonNull message carried");
        }
    }

    static void testRequireNonNullSupplier() {
        Object o = new Object();
        final boolean[] called = {false};
        check(Objects.requireNonNull(o, () -> { called[0] = true; return "never"; }) == o,
              "requireNonNull(supplier) returns object");
        check(!called[0], "supplier not called when non-null");

        try {
            Objects.requireNonNull(null, () -> "supplied");
            throw new AssertionError("expected NPE");
        } catch (NullPointerException e) {
            check("supplied".equals(e.getMessage()), "requireNonNull(supplier) message carried");
        }

        // null supplier -> NPE from supplier.get() (only when obj is null).
        expectThrows(NullPointerException.class,
            () -> Objects.requireNonNull(null, (Supplier<String>) null),
            "requireNonNull(null, null supplier) NPE");
        // supplier returns null message -> NPE with null message.
        try {
            Objects.requireNonNull(null, () -> null);
            throw new AssertionError("expected NPE");
        } catch (NullPointerException e) {
            check(e.getMessage() == null, "requireNonNull(supplier null message) message null");
        }
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

        // nested arrays.
        Object[] nested1 = {new int[]{1, 2}, new String[]{"a", null}};
        Object[] nested2 = {new int[]{1, 2}, new String[]{"a", null}};
        check(Objects.deepEquals(nested1, nested2), "deepEquals nested arrays with null element");
        check(!Objects.deepEquals(nested1, new Object[]{new int[]{1, 2}, new String[]{"a", "b"}}),
              "deepEquals nested arrays differ");

        // primitive multi-dim.
        int[][] m1 = {{1, 2}, {3, 4}};
        int[][] m2 = {{1, 2}, {3, 4}};
        check(Objects.deepEquals(m1, m2), "deepEquals 2D int arrays");
        check(!Objects.deepEquals(m1, new int[][]{{1, 2}, {3, 5}}), "deepEquals 2D differ");

        // array vs non-array.
        check(!Objects.deepEquals(new int[]{1}, 1), "deepEquals array vs scalar false");
        check(!Objects.deepEquals(new Object[]{"x"}, "x"), "deepEquals Object[] vs String false");
        // int[] vs Integer[] with same values is false (runtime array types differ).
        check(!Objects.deepEquals(new int[]{1, 2}, new Integer[]{1, 2}),
              "deepEquals int[] vs Integer[] false");
    }

    static void testHashCode() {
        check(Objects.hashCode(null) == 0, "hashCode(null) == 0");
        check(Objects.hashCode("x") == "x".hashCode(), "hashCode delegates");
        check(Objects.hash("a", "b") == Objects.hash("a", "b"), "hash varargs stable");
        check(Objects.hash() == 1, "hash() == 1");
        // hash == Arrays.hashCode of the same sequence, incl. null elements.
        check(Objects.hash("k", null, "v") == Arrays.hashCode(new Object[]{"k", null, "v"}),
              "hash == Arrays.hashCode with null element");
        check(Objects.hash((Object) null) == Arrays.hashCode(new Object[]{null}),
              "hash single null == Arrays.hashCode");
        // null varargs array itself -> Arrays.hashCode(null) == 0.
        check(Objects.hash((Object[]) null) == 0, "hash(null varargs array) == 0");
    }

    static void testToString() {
        check(Objects.toString(null).equals("null"), "toString(null) == \"null\"");
        check(Objects.toString("x").equals("x"), "toString delegates");
        check(Objects.toString(null, "dflt").equals("dflt"), "toString(null, dflt)");
        check(Objects.toString("x", "dflt").equals("x"), "toString(non-null, dflt)");
        // toString(o) delegates to String.valueOf: an object whose toString() returns null
        // yields null (not "null").
        Object nullToString = new Object() { public String toString() { return null; } };
        check(Objects.toString(nullToString) == null, "toString(object with null toString) == null");
    }

    static void testCompare() {
        check(Objects.compare("a", "b", String::compareTo) < 0, "compare < 0");
        check(Objects.compare("b", "a", String::compareTo) > 0, "compare > 0");
        check(Objects.compare("a", "a", String::compareTo) == 0, "compare == 0");
        // null matrix: (null,null) == 0 without invoking comparator.
        check(Objects.compare(null, null, String::compareTo) == 0, "compare(null,null) == 0");
        // null vs non-null delegates to comparator (String::compareTo will NPE on null).
        expectThrows(NullPointerException.class,
            () -> Objects.compare(null, "x", String::compareTo),
            "compare(null,x) delegates -> comparator NPE");
        expectThrows(NullPointerException.class,
            () -> Objects.compare("x", null, String::compareTo),
            "compare(x,null) delegates -> comparator NPE");
        // null comparator with distinct args -> NPE.
        expectThrows(NullPointerException.class,
            () -> Objects.compare("a", "b", null), "compare(a,b,null) NPE");
        // identical args short-circuit even with null comparator.
        check(Objects.compare("a", "a", null) == 0, "compare(a,a,null) == 0 (short-circuit)");
        // antisymmetry.
        check(Objects.compare("a", "b", String::compareTo)
                == -Objects.compare("b", "a", String::compareTo), "compare antisymmetry");
    }

    static void testIsNull() {
        check(Objects.isNull(null), "isNull(null)");
        check(!Objects.isNull("x"), "!isNull(x)");
        check(Objects.nonNull("x"), "nonNull(x)");
        check(!Objects.nonNull(null), "!nonNull(null)");
    }

    public static void main(String[] args) {
        testRequireNonNull();
        testRequireNonNullSupplier();
        testEquals();
        testDeepEquals();
        testHashCode();
        testToString();
        testCompare();
        testIsNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
