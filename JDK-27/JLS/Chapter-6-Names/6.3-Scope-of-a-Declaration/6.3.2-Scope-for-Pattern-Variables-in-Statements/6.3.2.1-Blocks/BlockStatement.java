/*
 * @test
 * @summary JLS 6.3.2.1: a pattern variable introduced by a statement S in a block
 *          is definitely matched at all the block statements following S.
 * @run main BlockStatement
 */

public class BlockStatement {
    static void requireString(Object o) {
        if (!(o instanceof String s)) {
            throw new IllegalArgumentException();
        }
        // s is in scope for the rest of the block.
        if (!s.equals("ok")) throw new AssertionError("s in scope after if");
    }
    public static void main(String[] args) {
        requireString("ok");
        try {
            requireString(42);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        System.out.println("PASS");
    }
}
