/*
 * @test
 * @summary JLS 5.1.1: a conversion from a type to that same type is permitted
 *          for any type; redundant cast operators are permitted.
 * @run main IdentityConversion
 */

public class IdentityConversion {
    public static void main(String[] args) {
        int i = 42;
        int j = i;                    // identity conversion (int -> int)
        if (j != 42) throw new AssertionError("int identity");

        // A redundant cast is permitted (identity conversion via cast).
        int k = (int) i;
        if (k != 42) throw new AssertionError("redundant int cast");

        Object o = "hello";
        Object p = o;                 // identity (Object -> Object)
        String s = (String) o;        // (reference, not identity) but also permitted
        if (p != o) throw new AssertionError("Object identity");
        if (!s.equals("hello")) throw new AssertionError("String cast");

        // Identity for array types.
        int[] a = new int[1];
        int[] b = a;
        if (b != a) throw new AssertionError("array identity");

        System.out.println("PASS");
    }
}
