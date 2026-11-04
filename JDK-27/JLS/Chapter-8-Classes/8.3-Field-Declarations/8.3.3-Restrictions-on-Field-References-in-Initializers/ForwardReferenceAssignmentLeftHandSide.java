/*
 * @test
 * @summary JLS 8.3.3: a reference that is on the left hand side of an assignment
 *          is not a forbidden forward reference.
 * @run main ForwardReferenceAssignmentLeftHandSide
 */
public class ForwardReferenceAssignmentLeftHandSide {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static { x = 100; }
    static int x;
    { y = 200; }
    int y;
    public static void main(String[] args) {
        check(x == 100, "static initializer assignment to later field");
        check(new ForwardReferenceAssignmentLeftHandSide().y == 200, "instance initializer assignment to later field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
