/*
 * @test
 * @summary JLS 6.4 Example 6.4-1 Test3: local variables with the same name may
 *          be declared in two separate for statements, neither containing the
 *          other.
 * @run main SeparateBlocksSameName
 */

public class SeparateBlocksSameName {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i);
        for (int i = 3; i > 0; i--) sb.append(i);
        if (!sb.toString().equals("012321")) throw new AssertionError(sb.toString());

        System.out.println("PASS");
    }
}
