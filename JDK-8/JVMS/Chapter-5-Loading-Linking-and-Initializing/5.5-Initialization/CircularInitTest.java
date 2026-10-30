/*
 * @test
 * @summary JVMS 5.5 (Java SE 8): a recursive request for initialization by the
 *          current thread completes normally instead of re-entering the
 *          initializer.  A <-> B circular dependency therefore resolves with
 *          both fields reading the not-yet-assigned default value 0, without
 *          deadlock or infinite recursion.
 * @run main CircularInitTest
 */

public class CircularInitTest {
    static class A { static int a = B.b; }
    static class B { static int b = A.a; }

    public static void main(String[] args) {
        // Accessing A.a starts A's init, which reads B.b (starts B's init), which
        // reads A.a; A is already in progress so the default 0 is read.  Both
        // fields therefore end up 0.
        if (A.a != 0) throw new AssertionError("A.a expected 0, got " + A.a);
        if (B.b != 0) throw new AssertionError("B.b expected 0, got " + B.b);

        System.out.println("PASS");
    }
}
