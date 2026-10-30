/*
 * @test
 * @summary JLS 15.25/15.26 (Java SE 8): the conditional operator selects one
 *          branch; compound assignment operators apply an implicit cast to the
 *          left-hand variable's type.
 * @run main ConditionalTest
 */

public class ConditionalTest {
    public static void main(String[] args) {
        int a = (true) ? 1 : 2;
        int b = (false) ? 1 : 2;
        if (a != 1 || b != 2) throw new AssertionError("conditional");

        byte x = 100;
        x += 100;   // implicit cast to byte: 200 -> -56
        if (x != -56) throw new AssertionError("compound assignment cast");

        int y = 10;
        y *= 3;
        if (y != 30) throw new AssertionError("compound assignment");

        System.out.println("PASS");
    }
}
