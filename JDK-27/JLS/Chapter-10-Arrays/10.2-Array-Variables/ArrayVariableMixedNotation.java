/*
 * @test
 * @summary JLS 10.2 (Example 10.2-2): bracket pairs may appear on both the type
 *          and in declarators; the array type of each variable is the combination
 *          of the element type, declarator brackets, and type brackets.
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
        matrix[0] = new byte[4];

        // rowvector/colvector are byte[]; matrix is byte[][].
        check(rowvector instanceof byte[], "rowvector is byte[]");
        check(colvector instanceof byte[], "colvector is byte[]");
        check(matrix instanceof byte[][], "matrix is byte[][]");
        check(rowvector.length == 1, "rowvector length");
        check(colvector.length == 2, "colvector length");
        check(matrix.length == 3, "matrix length");

        int a, b[], c[][];
        a = 1;
        b = new int[2];
        c = new int[3][4];
        check(b instanceof int[], "b is int[]");
        check(c instanceof int[][], "c is int[][]");

        // The pathological mixed declaration from the specification.
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
