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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
