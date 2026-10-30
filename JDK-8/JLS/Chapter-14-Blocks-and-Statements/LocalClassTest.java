/*
 * @test
 * @summary JLS 14.3 (Java SE 8): a local class may be declared in a block and may
 *          access final or effectively final local variables of the enclosing scope.
 * @run main LocalClassTest
 */

public class LocalClassTest {
    public static void main(String[] args) {
        final int x = 5;
        int y = 7;   // effectively final
        class Local {
            int sum() { return x + y; }
        }
        Local l = new Local();
        if (l.sum() != 12) throw new AssertionError("local class captures locals");
        System.out.println("PASS");
    }
}
