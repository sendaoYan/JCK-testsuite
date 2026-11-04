/*
 * @test
 * @summary java.util.Objects API conformance (Java 8 surface): complete contract coverage
 *          of requireNonNull (String + Supplier variants, null-supplier/supplier-null-message),
 *          equals, deepEquals (nested/primitive/null-element/array-vs-non-array), hashCode/hash
 *          (null elements / null varargs array), toString (toString()-returns-null edge),
 *          compare (null matrix + null comparator), isNull/nonNull.
 * @run main ObjectsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    // ---- deepened contract coverage, round 2 (empirically locked on jdk8u372) --

    static void testHashFormulas() {
        // Objects.hash delegates to Arrays.hashCode: result=1; result=31*result+e.hashCode()
        check(Objects.hash("a", "b") == 4066, "hash(a,b) hand-computed 4066");
        check(Objects.hash("b", "a") == 4096, "hash(b,a) order-sensitive 4096");
        check(Objects.hash("a") == 128, "hash(a) == 31*1+97");
        check(Objects.hash((Object) null) == 31, "hash(single null) == 31*1+0");
        check(Objects.hash("a", null) == 31 * (31 * 1 + 97) + 0, "hash(a, null) formula");
        check(Objects.hash("x", "y") != Objects.hash("x", "z"), "hash varies with elements");
        Object o = new Object();
        check(Objects.hashCode(o) == o.hashCode(), "hashCode delegates identity for plain Object");
        check(Objects.hashCode(o) == Objects.hashCode(o), "hashCode stable across calls");
    }

    static void testEqualsMore() {
        // identity shortcut: x.equals(x) true even without invoking Object.equals override
        check(Objects.equals(Integer.valueOf(5), Integer.valueOf(5)), "equals equal values true");
        check(!Objects.equals(Integer.valueOf(1), "1"), "equals different types false");
        check(!Objects.equals(new ArrayList<String>(), new HashSet<String>()),
              "equals across unrelated collection types false");
        check(Objects.equals(new ArrayList<String>(Arrays.asList("a")),
                             new ArrayList<String>(Arrays.asList("a"))),
              "equals ArrayList same content true");
        // symmetry with the null-first argument
        check(!Objects.equals(new int[]{1}, null) && !Objects.equals(null, new int[]{1}),
              "equals array vs null false both directions");
    }

    static void testDeepEqualsMore() {
        check(!Objects.deepEquals(null, new Object[]{null}), "deepEquals(null, {null}) false");
        check(!Objects.deepEquals(new Object[]{null}, null), "deepEquals({null}, null) false");
        int[] self = {1, 2};
        check(Objects.deepEquals(self, self), "deepEquals same reference true (== shortcut)");
        check(Objects.deepEquals(new Object[0], new Object[0]), "deepEquals empty object arrays true");
        // 2-level nesting mixing Object[] and primitive arrays
        Object[] n1 = {new Object[]{new int[]{7}}, "leaf", null};
        Object[] n2 = {new Object[]{new int[]{7}}, "leaf", null};
        check(Objects.deepEquals(n1, n2), "deepEquals two-level nesting with null leaf");
        n2[1] = "other";
        check(!Objects.deepEquals(n1, n2), "deepEquals two-level nesting differs");
        // deepHashCode is stable across deep-equal structures
        Object[] n1clone = {new Object[]{new int[]{7}}, "leaf", null};
        check(Arrays.deepHashCode(n1) == Arrays.deepHashCode(n1clone),
              "deepHashCode stable across deep-equal structures");
    }

    static void testToStringMore() {
        check(Objects.toString(null, null) == null, "toString(null, null) returns null");
        check(Objects.toString(null, "").equals(""), "toString(null, empty default)");
        Object o = new Object();
        check(Objects.toString(o).equals(o.toString()), "toString delegates");
        check(Objects.toString(o, "dflt").equals(o.toString()), "toString ignores default for non-null");
    }

    static void testRequireNonNullMore() {
        Object o = new Object();
        // null message String is allowed as long as the object is non-null
        check(Objects.requireNonNull(o, (String) null) == o, "requireNonNull(o, null msg) ok");
        try {
            Objects.requireNonNull(null, (String) null);
            throw new AssertionError("expected NPE");
        } catch (NullPointerException e) {
            check(e.getMessage() == null, "requireNonNull(null, null msg) NPE with null message");
        }
    }

    static void testCompareRawDelegate() {
        // Objects.compare returns c.compare(a,b) as-is (only the sign is specified, but the
        // delegate passthrough is the implementation contract)
        java.util.Comparator<String> raw = (x, y) -> x.length() * 7 - y.length() * 3;
        check(Objects.compare("aa", "b", raw) == raw.compare("aa", "b"),
              "compare passes comparator return through unchanged");
        check(Objects.compare("aa", "b", raw) > 0, "compare sign follows comparator");
        // antisymmetry across a matrix
        String[] words = {"a", "bb", "cc", ""};
        for (String x : words) for (String y : words)
            check(Integer.signum(Objects.compare(x, y, String::compareTo))
                        == -Integer.signum(Objects.compare(y, x, String::compareTo)),
                  "compare antisymmetry " + x.length() + "," + y.length());
    }

    static void testNullPredicates() {
        // isNull/nonNull are usable as Predicate function objects
        java.util.function.Predicate<Object> isNull = Objects::isNull;
        java.util.function.Predicate<Object> nonNull = Objects::nonNull;
        check(isNull.test(null) && !isNull.test("x"), "isNull as Predicate");
        check(nonNull.test("x") && !nonNull.test(null), "nonNull as Predicate");
        check(isNull.negate().test("x"), "isNull.negate behaves as nonNull");
        java.util.List<Object> withNulls = new ArrayList<>(Arrays.asList("a", null, "b", null));
        withNulls.removeIf(Objects::isNull);
        check(withNulls.equals(Arrays.asList("a", "b")), "removeIf(Objects::isNull) filters nulls");
    }

    static void testCompareTransitivityMatrix() {
        // transitivity of the >-relation induced by a delegate comparator
        String[] words = {"", "a", "ab", "z"};
        for (String x : words) for (String y : words) for (String z : words)
            check(!(Objects.compare(x, y, String::compareTo) > 0
                 && Objects.compare(y, z, String::compareTo) > 0)
              || Objects.compare(x, z, String::compareTo) > 0,
                  "compare transitivity " + x + "," + y + "," + z);
    }

    static void testHashOnContainers() {
        List<String> l = Arrays.asList("a", "b");
        check(Objects.hash(l) == 31 + l.hashCode(), "hash(list) wraps as single varargs element");
        // array element inside varargs: uses the element's own hashCode (identity for arrays), not deep
        int[] arr = {1, 2};
        check(Objects.hash((Object) arr) == 31 * 1 + arr.hashCode(), "hash(array element) not deep");
        check(Objects.hash((Object) arr, "x") == 31 * (31 * 1 + arr.hashCode()) + "x".hashCode(),
              "hash formula with array element");
        check(Objects.hashCode(1.0) == Double.valueOf(1.0).hashCode(), "hashCode(Double) delegates");
        check(Objects.hashCode(true) == Boolean.TRUE.hashCode(), "hashCode(Boolean) delegates");
        check(Objects.hash("a", 1, true) == Arrays.hashCode(new Object[]{"a", 1, true}),
              "hash mixed types == Arrays.hashCode");
    }

    static void testEqualsIdentitySemantics() {
        Object o = new Object();
        check(Objects.equals(o, o), "equals(x,x) true");
        check(Objects.equals(Double.NaN, Double.NaN), "equals NaN via Double.equals true");
        // plain equals on arrays is identity, NOT element-wise (deepEquals is for that)
        check(!Objects.equals(new int[]{1}, new int[]{1}), "equals on distinct arrays false");
        int[] a = {1};
        check(Objects.equals(a, a), "equals same array reference true");
        check(Objects.equals(new Object[]{"x"}, new Object[]{"x"}) == false,
              "equals on distinct Object[] false");
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
        testHashFormulas();
        testEqualsMore();
        testDeepEqualsMore();
        testToStringMore();
        testRequireNonNullMore();
        testCompareRawDelegate();
        testNullPredicates();
        testCompareTransitivityMatrix();
        testHashOnContainers();
        testEqualsIdentitySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
