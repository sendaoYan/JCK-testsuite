/*
 * @test
 * @summary JLS 8.10.1: a record component may be a variable arity record
 *          component; its declared type is an array type.
 * @run main VariableArityRecordComponent
 */

public record VariableArityRecordComponent(int[]... xs) {
    public static void main(String[] args) {
        VariableArityRecordComponent r =
            new VariableArityRecordComponent(new int[]{1, 2}, new int[]{3});
        int[][] all = r.xs();
        if (all.length != 2) throw new AssertionError("length: " + all.length);
        if (all[0].length != 2 || all[0][0] != 1 || all[0][1] != 2)
            throw new AssertionError("first array");
        if (all[1].length != 1 || all[1][0] != 3)
            throw new AssertionError("second array");
        System.out.println("PASS");
    }
}
