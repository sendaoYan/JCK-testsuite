/*
 * @test
 * @summary JLS 16.2.7: a variable assigned in both arms of an if-else statement
 *          is definitely assigned after the statement.
 * @run main IfElseDA
 */

public class IfElseDA {
    public static void main(String[] args) {
        int x;
        if (true) x = 1; else x = 2;
        if (x != 1) throw new AssertionError("if-else x == " + x);

        int y;
        if (false) y = 1; else y = 2;
        if (y != 2) throw new AssertionError("if-else y == " + y);

        int z;
        if (false) z = 1; else if (true) z = 2; else z = 3;
        if (z != 2) throw new AssertionError("if-else-if z == " + z);

        System.out.println("PASS");
    }
}
