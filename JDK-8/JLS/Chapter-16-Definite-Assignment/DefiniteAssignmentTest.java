/*
 * @test
 * @summary JLS 16 (Java SE 8): a local variable is definitely assigned after a
 *          value is assigned on every possible path; a blank final may be assigned
 *          once before use.
 * @run main DefiniteAssignmentTest
 */

public class DefiniteAssignmentTest {
    public static void main(String[] args) {
        int x;
        boolean b = true;
        if (b) x = 1; else x = 2;
        if (x != 1) throw new AssertionError("x definitely assigned on both branches");

        final int y;
        y = 5;
        if (y != 5) throw new AssertionError("blank final assigned once");
        System.out.println("PASS");
    }
}
