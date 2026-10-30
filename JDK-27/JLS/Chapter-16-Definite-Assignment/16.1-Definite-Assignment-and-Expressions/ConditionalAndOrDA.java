/*
 * @test
 * @summary JLS 16.1.2 / 16.1.3: the right operand of && and || is assigned into
 *          only when it is evaluated, which depends on the left operand.
 * @run main ConditionalAndOrDA
 */

public class ConditionalAndOrDA {
    public static void main(String[] args) {
        int x;
        boolean t = true, f = false;

        // && : right operand is evaluated only when the left operand is true,
        // so x is definitely assigned in the then-branch.
        if (t && (x = 1) == 1) {
            if (x != 1) throw new AssertionError("&& then-branch x == " + x);
        } else {
            throw new AssertionError("&& should be true");
        }

        // || : right operand is evaluated only when the left operand is false.
        if (f || (x = 2) == 2) {
            if (x != 2) throw new AssertionError("|| right operand x == " + x);
        } else {
            throw new AssertionError("|| should be true");
        }

        System.out.println("PASS");
    }
}
