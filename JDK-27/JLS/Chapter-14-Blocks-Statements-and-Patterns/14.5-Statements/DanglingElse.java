/*
 * @test
 * @summary JLS 14.5: an else clause belongs to the innermost if that it could
 *          possibly belong to (the "dangling else" rule).
 * @run main DanglingElse
 */

public class DanglingElse {
    public static void main(String[] args) {
        boolean door = true;
        boolean resident = false;
        String msg = "none";

        if (door)
            if (resident)
                msg = "greet";
        else
            msg = "ring";   // belongs to the inner if(resident)

        // door==true, resident==false: inner if-then-else takes its else branch.
        if (!"ring".equals(msg))
            throw new AssertionError("else bound to wrong if: " + msg);

        System.out.println("PASS");
    }
}
