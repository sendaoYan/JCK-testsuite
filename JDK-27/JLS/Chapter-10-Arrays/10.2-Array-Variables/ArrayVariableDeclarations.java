/*
 * @test
 * @summary JLS 10.2 (Example 10.2-2): brackets in declarators are equivalent to
 *          brackets in the type; mixed notation yields the same array type.
 * @run main ArrayVariableDeclarations
 */

public class ArrayVariableDeclarations {
    public static void main(String[] args) {
        byte[] rowvector, colvector, matrix[];
        rowvector = new byte[1];
        colvector = new byte[2];
        matrix = new byte[3][4];

        if (rowvector.length != 1) throw new AssertionError("rowvector");
        if (colvector.length != 2) throw new AssertionError("colvector");
        if (matrix.length != 3 || matrix[0].length != 4) throw new AssertionError("matrix");

        // int a, b[], c[][] is equivalent to int a; int[] b; int[][] c.
        int a, b[], c[][];
        a = 1;
        b = new int[2];
        c = new int[3][4];
        if (a != 1 || b.length != 2 || c.length != 3 || c[0].length != 4)
            throw new AssertionError("mixed notation equivalence");
        System.out.println("PASS");
    }
}
