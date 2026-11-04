/*
 * @test
 * @summary JLS 4.12.4 (Java SE 27): a variable that is never assigned after its
 *          initializer (and is not declared final) is effectively final, so it
 *          may be captured by a lambda or local/anonymous class, and adding the
 *          final modifier would not introduce a compile-time error. A variable
 *          reassigned anywhere is not effectively final.
 * @run main EffectivelyFinal
 */

public class EffectivelyFinal {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface IntSupplier { int get(); }

    public static void main(String[] args) {
        // Effectively final local variable with an initializer.
        int a = 1;
        IntSupplier s1 = () -> a;
        check(s1.get() == 1, "effectively final local captured by lambda");

        // Effectively final local variable without an initializer (assigned once).
        int b;
        b = 2;
        IntSupplier s2 = () -> b;
        check(s2.get() == 2, "effectively final local (assigned once) captured by lambda");

        // Effectively final parameter.
        check(paramSupplier(3).get() == 3, "effectively final parameter captured by lambda");

        // A variable assigned in one branch is still effectively final (it is
        // never assigned after initialization on any path).
        int c = cond(true) ? 4 : 5;
        IntSupplier s3 = () -> c;
        check(s3.get() == 4, "branch-initialized effectively final local captured by lambda");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean cond(boolean v) { return v; }

    static IntSupplier paramSupplier(int p) {
        return () -> p;   // parameter p is effectively final
    }
}
