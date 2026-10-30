/*
 * @test
 * @summary JLS 5.2 (Java SE 8): assignment contexts apply implicit narrowing only
 *          for constant expressions representable in the target type, and
 *          implicit widening otherwise.
 * @run main AssignmentContexts
 */

public class AssignmentContexts {
    public static void main(String[] args) {
        byte b = 100;          // constant narrowing: 100 fits in byte
        short s = 300;
        char c = 65;
        if (b != 100 || s != 300 || c != 'A') throw new AssertionError("constant narrowing");

        int i = 1_000_000_000;
        long l = i;            // widening
        float f = 1L;          // long -> float widening
        if (l != 1000000000L || f != 1.0f) throw new AssertionError("widening");

        final int CONST = 10;
        byte bc = CONST;       // constant-variable narrowing
        if (bc != 10) throw new AssertionError("final constant narrowing");

        System.out.println("PASS");
    }
}
