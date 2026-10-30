/*
 * @test
 * @summary JLS 10.6: a trailing comma may appear after the last expression in an
 *          array initializer and is ignored; an empty initializer is allowed.
 * @run main ArrayInitializerTrailingComma
 */

public class ArrayInitializerTrailingComma {
    public static void main(String[] args) {
        String[] aas = { "array", "of", "String", };
        if (aas.length != 3 || !"String".equals(aas[2]))
            throw new AssertionError("trailing comma");

        int[] empty = { };
        if (empty.length != 0) throw new AssertionError("empty initializer");
        System.out.println("PASS");
    }
}
