/*
 * @test
 * @summary JLS 6.5.6.1 Example 6.5.6.1-1: a definitely-assigned final variable
 *          denotes its value; a non-final variable denotes the variable.
 * @run main FinalVariableIsAValue
 */

public class FinalVariableIsAValue {
    static int v;
    static final int f = 3;
    public static void main(String[] args) {
        int i;
        i = 1;
        v = 2;
        if (i != 1 || v != 2 || f != 3) throw new AssertionError("simple expression names");

        System.out.println("PASS");
    }
}
