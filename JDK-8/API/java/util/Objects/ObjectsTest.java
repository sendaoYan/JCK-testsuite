/*
 * @test
 * @summary java.util.Objects API conformance (Java 8 surface): requireNonNull (with and
 *          without message, returns the object), equals (null-safe), deepEquals (arrays),
 *          hashCode (0 for null), toString (o.toString() / "null"), compare, isNull/nonNull.
 * @run main ObjectsTest
 */

import java.util.Objects;

public class ObjectsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRequireNonNull() {
        Object o = new Object();
        check(Objects.requireNonNull(o) == o, "requireNonNull returns object");
        check(Objects.requireNonNull(o, "msg") == o, "requireNonNull(msg) returns object");
        try {
            Objects.requireNonNull(null);
            throw new AssertionError("requireNonNull(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            Objects.requireNonNull(null, "boom");
            throw new AssertionError("requireNonNull(null,msg): expected NullPointerException");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("boom"), "requireNonNull message carried");
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
    }

    static void testHashCode() {
        check(Objects.hashCode(null) == 0, "hashCode(null) == 0");
        check(Objects.hashCode("x") == "x".hashCode(), "hashCode delegates");
        // varargs hash produces a stable value, and null-safe.
        check(Objects.hash("a", "b") == Objects.hash("a", "b"), "hash varargs stable");
        // Objects.hash() delegates to Arrays.hashCode of an empty array, which is 1.
        check(Objects.hash() == 1, "hash() == 1");
    }

    static void testToString() {
        check(Objects.toString(null).equals("null"), "toString(null) == \"null\"");
        check(Objects.toString("x").equals("x"), "toString delegates");
        check(Objects.toString(null, "dflt").equals("dflt"), "toString(null, dflt)");
        check(Objects.toString("x", "dflt").equals("x"), "toString(non-null, dflt)");
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

    public static void main(String[] args) {
        testRequireNonNull();
        testEquals();
        testDeepEquals();
        testHashCode();
        testToString();
        testCompare();
        testIsNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
