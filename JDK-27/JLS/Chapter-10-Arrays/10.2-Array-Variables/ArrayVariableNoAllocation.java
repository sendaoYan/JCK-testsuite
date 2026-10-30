/*
 * @test
 * @summary JLS 10.2: declaring an array variable does not create an array object
 *          or allocate space for its components; the variable initially holds null.
 * @run main ArrayVariableNoAllocation
 */

public class ArrayVariableNoAllocation {
    static int[] field;   // default value null, no array allocated

    public static void main(String[] args) {
        if (field != null) throw new AssertionError("declaration allocated an array");
        int[] local = null;
        if (local != null) throw new AssertionError("local declaration allocated an array");
        System.out.println("PASS");
    }
}
