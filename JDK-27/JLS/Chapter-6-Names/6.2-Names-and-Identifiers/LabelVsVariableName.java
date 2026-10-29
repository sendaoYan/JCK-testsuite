/*
 * @test
 * @summary JLS 6.2: identifiers used in labeled statements and their break/
 *          continue statements are completely separate from identifiers used in
 *          declarations, so a label may share a name with a variable.
 * @run main LabelVsVariableName
 */

public class LabelVsVariableName {
    public static void main(String[] args) {
        int count = 0;
        // Label "count" is separate from the local variable "count".
        count: for (int i = 0; i < 5; i++) {
            count++;              // refers to the variable
            if (i == 3) break count;  // refers to the label
        }
        if (count != 4) throw new AssertionError("label/variable separation, got " + count);

        System.out.println("PASS");
    }
}
