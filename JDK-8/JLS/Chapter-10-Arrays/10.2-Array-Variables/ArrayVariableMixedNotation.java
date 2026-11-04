/*
 * @test
 * @summary JLS 10.2 (Java SE 8, Example 10.2-2): bracket pairs may appear on both
 *          the type and in declarators; the array type is the combination of the
 *          element type, declarator brackets, and type brackets.
 * @run main ArrayVariableMixedNotation
 */

public class ArrayVariableMixedNotation {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        byte[] rowvector, colvector, matrix[];
        rowvector = new byte[1];
        colvector = new byte[2];
        matrix = new byte[3][];
        check(rowvector instanceof byte[], "rowvector is byte[]");
        check(colvector instanceof byte[], "colvector is byte[]");
        check(matrix instanceof byte[][], "matrix is byte[][]");

        float[][] f[][], g[][][], h[];
        f = new float[2][3][][];
        g = new float[2][3][4][][];
        h = new float[2][3][];
        check(f instanceof float[][][][], "f is float[][][][]");
        check(g instanceof float[][][][][], "g is float[][][][][]");
        check(h instanceof float[][][], "h is float[][][]");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
