/*
 * @test
 * @summary JLS 15.22-15.24 (Java SE 8): bitwise and logical operators, including
 *          short-circuit evaluation of && and ||.
 * @run main LogicalBitwiseTest
 */

public class LogicalBitwiseTest {
    static boolean called = false;
    static boolean f(boolean v) { called = true; return v; }

    public static void main(String[] args) {
        if ((true || f(true)) != true) throw new AssertionError("|| result");
        if (called) throw new AssertionError("|| short-circuits");

        called = false;
        if ((false && f(false)) != false) throw new AssertionError("&& result");
        if (called) throw new AssertionError("&& short-circuits");

        // Non-short-circuit bitwise on booleans evaluates both operands.
        called = false;
        boolean r = false & f(true);
        if (r != false || !called) throw new AssertionError("& evaluates both");

        System.out.println("PASS");
    }
}
