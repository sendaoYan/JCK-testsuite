/*
 * @test
 * @summary JLS 16.2.12: definite assignment across a basic for statement; a
 *          variable assigned in the initialization part is definitely assigned
 *          after the statement.
 * @run main ForLoopDA
 */

public class ForLoopDA {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 3; i++) sum += i;
        if (sum != 3) throw new AssertionError("for sum == " + sum);

        int i = 0;
        int count = 0;
        for (; i < 2; i++) count++;
        // i was assigned in the initialization part, so it is definitely assigned.
        if (i != 2 || count != 2) throw new AssertionError("for i/count");

        System.out.println("PASS");
    }
}
