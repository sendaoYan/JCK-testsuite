/*
 * @test
 * @summary JLS 16.1.8: after an assignment expression a = b the variable a is
 *          definitely assigned, including in chained assignments.
 * @run main AssignmentExpressionDA
 */

public class AssignmentExpressionDA {
    public static void main(String[] args) {
        int x, y;
        x = (y = 42);   // both x and y are definitely assigned after this
        if (x != 42 || y != 42) throw new AssertionError("chained assignment");

        System.out.println("PASS");
    }
}
