/*
 * @test
 * @summary JLS 15.7.1 (Example 15.7.1-3): if the left-hand operand completes
 *          abruptly, no part of the right-hand operand is evaluated.
 * @run main LeftHandAbruptCompletion
 */

public class LeftHandAbruptCompletion {
    static int forgetIt() throws Exception { throw new Exception("outta"); }

    public static void main(String[] args) throws Exception {
        int j = 1;
        try {
            int i = forgetIt() / (j = 2);
            throw new AssertionError("should have thrown");
        } catch (Exception e) {
            if (!"outta".equals(e.getMessage()))
                throw new AssertionError("wrong exception: " + e.getMessage());
        }
        if (j != 1) throw new AssertionError("right operand evaluated: j == " + j);
        System.out.println("PASS");
    }
}
