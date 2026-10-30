/*
 * @test
 * @summary JLS 16.9: a blank final instance field must be definitely assigned
 *          exactly once by the constructor.
 * @run main BlankFinalField
 */

public class BlankFinalField {
    final int f;

    BlankFinalField(int v) {
        f = v;   // blank final assigned exactly once
    }

    public static void main(String[] args) {
        if (new BlankFinalField(42).f != 42) throw new AssertionError("blank final");

        System.out.println("PASS");
    }
}
