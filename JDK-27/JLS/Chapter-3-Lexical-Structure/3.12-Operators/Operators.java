/*
 * @test
 * @summary JLS 3.12: the 38 operators are recognized tokens and behave as the
 *          corresponding operators (their semantics are specified in chapter
 *          15); this test exercises each operator token in a valid context.
 * @run main Operators
 */

public class Operators {
    public static void main(String[] args) {
        int a = 10, b = 3;
        boolean ok = true;

        // Arithmetic: + - * / %
        ok &= (a + b == 13) && (a - b == 7) && (a * b == 30)
              && (a / b == 3) && (a % b == 1);

        // Bitwise: & | ^ ~ << >> >>>
        ok &= ((a & b) == 2) && ((a | b) == 11) && ((a ^ b) == 9)
              && (~a == -11) && (a << 1 == 20) && (a >> 1 == 5) && (a >>> 1 == 5);
        ok &= (-1 >>> 1 == Integer.MAX_VALUE);

        // Comparison: > < >= <= == !=
        ok &= (a > b) && (b < a) && (a >= b) && (b <= a) && (a == 10) && (b != 10);

        // Logical: && || !
        ok &= (true && true) && (false || true) && (!false);

        // Conditional: ? :
        ok &= (a > b ? 1 : 2) == 1;

        // Lambda arrow: ->
        Runnable r = () -> { };
        r.run();

        // Assignment: = += -= *= /= %= &= |= ^= <<= >>= >>>=
        int v = 10;
        v += 1; ok &= (v == 11);
        v -= 1; ok &= (v == 10);
        v *= 2; ok &= (v == 20);
        v /= 2; ok &= (v == 10);
        v %= 7; ok &= (v == 3);
        v = 6; v &= 3; ok &= (v == 2);
        v |= 4; ok &= (v == 6);
        v ^= 3; ok &= (v == 5);
        v <<= 1; ok &= (v == 10);
        v >>= 1; ok &= (v == 5);
        v >>>= 1; ok &= (v == 2);

        // Increment / decrement: ++ --
        int p = 0;
        ok &= (p++ == 0) && (p == 1);
        ok &= (++p == 2);
        ok &= (p-- == 2) && (p == 1);
        ok &= (--p == 0);

        if (!ok) {
            throw new AssertionError("an operator token did not behave as specified");
        }
        System.out.println("PASS");
    }
}
