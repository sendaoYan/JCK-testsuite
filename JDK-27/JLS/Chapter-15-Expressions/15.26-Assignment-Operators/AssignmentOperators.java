/*
 * @test
 * @summary JLS 15.26: simple assignment is an expression yielding the assigned
 *          value; compound assignment operators perform the binary operation and
 *          an implicit cast back to the variable's type.
 * @run main AssignmentOperators
 */

public class AssignmentOperators {
    public static void main(String[] args) {
        int x;
        x = 42;
        if (x != 42) throw new AssertionError("simple assignment");

        int y = (x = 10);          // assignment expression has a value
        if (y != 10 || x != 10) throw new AssertionError("assignment value");

        int z = 5;
        z += 3; if (z != 8) throw new AssertionError("+=");
        z *= 2; if (z != 16) throw new AssertionError("*=");
        z -= 6; if (z != 10) throw new AssertionError("-=");
        z /= 2; if (z != 5) throw new AssertionError("/=");
        z %= 3; if (z != 2) throw new AssertionError("%=");
        z <<= 1; if (z != 4) throw new AssertionError("<<=");
        z >>= 1; if (z != 2) throw new AssertionError(">>=");
        z >>>= 1; if (z != 1) throw new AssertionError(">>>=");
        z &= 0; if (z != 0) throw new AssertionError("&=");
        z |= 5; if (z != 5) throw new AssertionError("|=");
        z ^= 3; if (z != 6) throw new AssertionError("^=");

        System.out.println("PASS");
    }
}
