/*
 * @test
 * @summary JLS 15.14/15.15 (Java SE 8): prefix and postfix increment/decrement
 *          differ in the value the expression yields.
 * @run main IncrementDecrementTest
 */

public class IncrementDecrementTest {
    public static void main(String[] args) {
        int a = 5;
        int pre = ++a;   // a becomes 6, pre = 6
        int post = a++;  // post = 6, a becomes 7
        if (a != 7 || pre != 6 || post != 6) throw new AssertionError("prefix/postfix");

        int b = 10;
        int d = --b;     // b = 9, d = 9
        int e = b--;     // e = 9, b = 8
        if (b != 8 || d != 9 || e != 9) throw new AssertionError("decrement");

        System.out.println("PASS");
    }
}
