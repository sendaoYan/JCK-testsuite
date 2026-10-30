/*
 * @test
 * @summary JLS 15.26.2: a compound assignment to a narrow type performs an
 *          implicit narrowing cast of the result, unlike a simple assignment.
 * @run main CompoundAssignmentCast
 */

public class CompoundAssignmentCast {
    public static void main(String[] args) {
        byte b = 100;
        b += 200;               // implicit cast: (byte)(100 + 200) == (byte)300 == 44
        if (b != 44) throw new AssertionError("b == " + b);

        byte c = 100;
        c = (byte) (c + 200);   // equivalent explicit cast
        if (c != 44) throw new AssertionError("c == " + c);

        System.out.println("PASS");
    }
}
