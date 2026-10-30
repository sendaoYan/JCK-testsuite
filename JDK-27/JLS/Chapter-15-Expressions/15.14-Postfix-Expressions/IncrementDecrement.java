/*
 * @test
 * @summary JLS 15.14 / 15.15: postfix and prefix increment/decrement return the
 *          old (postfix) or new (prefix) value while updating the variable.
 * @run main IncrementDecrement
 */

public class IncrementDecrement {
    public static void main(String[] args) {
        int i = 5;
        int a = i++;          // postfix: a = 5, i = 6
        if (a != 5 || i != 6) throw new AssertionError("i++");

        int b = ++i;          // prefix: b = 7, i = 7
        if (b != 7 || i != 7) throw new AssertionError("++i");

        int c = i--;          // postfix: c = 7, i = 6
        if (c != 7 || i != 6) throw new AssertionError("i--");

        int d = --i;          // prefix: d = 5, i = 5
        if (d != 5 || i != 5) throw new AssertionError("--i");

        System.out.println("PASS");
    }
}
