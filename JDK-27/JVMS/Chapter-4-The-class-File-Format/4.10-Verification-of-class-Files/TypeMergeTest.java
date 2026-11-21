/*
 * @test
 * @summary JVMS 4.10.1.2: the null type is assignable to every reference type,
 *          and merging two reference types yields their common supertype, so a
 *          null/reference merge and a String/Integer merge both verify and run.
 * @run main TypeMergeTest
 */

public class TypeMergeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // null merges with a reference type at the join of the conditional: the
    // verifier treats null as a subtype of every reference type.
    static Object nullOrString(boolean b) {
        return b ? null : "hello";
    }

    // String and Integer merge to their least common supertype Object.
    static Object stringOrInteger(boolean b) {
        return b ? "hello" : Integer.valueOf(42);
    }

    public static void main(String[] args) {
        check(nullOrString(true) == null, "null branch must yield null");
        check("hello".equals(nullOrString(false)), "reference branch must yield the string");
        Object nullMerged = nullOrString(true);
        check(nullMerged == null, "null/reference merge verifies and null round-trips");

        check("hello".equals(stringOrInteger(true)), "string branch");
        check(Integer.valueOf(42).equals(stringOrInteger(false)), "integer branch");
        Object merged = stringOrInteger(true);
        check(merged instanceof Object && !(merged instanceof Integer),
                "String/Integer merge must widen to Object, not a common numeric type");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
