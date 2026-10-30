/*
 * @test
 * @summary JLS 16.1.9: the prefix and postfix increment/decrement operators
 *          definitely assign their operand.
 * @run main IncrementDecrementDA
 */

public class IncrementDecrementDA {
    public static void main(String[] args) {
        int x = 0;
        x++;
        if (x != 1) throw new AssertionError("x++");
        ++x;
        if (x != 2) throw new AssertionError("++x");

        int y = x--;   // y == 2, then x == 1
        if (y != 2 || x != 1) throw new AssertionError("x--");

        int z = --x;   // z == 0, then x == 0
        if (z != 0 || x != 0) throw new AssertionError("--x");

        System.out.println("PASS");
    }
}
