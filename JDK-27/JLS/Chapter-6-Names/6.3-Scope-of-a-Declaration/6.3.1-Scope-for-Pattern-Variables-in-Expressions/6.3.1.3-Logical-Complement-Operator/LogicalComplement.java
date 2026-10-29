/*
 * @test
 * @summary JLS 6.3.1.3: !a introduces a pattern variable when true iff a
 *          introduces it when false, and when false iff a introduces it when
 *          true. So !(o instanceof String s) makes s available in the branch
 *          where the whole expression is false.
 * @run main LogicalComplement
 */

public class LogicalComplement {
    static int lengthIfString(Object o) {
        // s is definitely matched when (!(o instanceof String s)) is false.
        return (!(o instanceof String s)) ? -1 : s.length();
    }
    public static void main(String[] args) {
        if (lengthIfString(42) != -1) throw new AssertionError("non-string");
        if (lengthIfString("abcd") != 4) throw new AssertionError("string");

        System.out.println("PASS");
    }
}
