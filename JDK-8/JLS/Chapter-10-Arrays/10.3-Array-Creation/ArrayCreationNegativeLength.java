/*
 * @test
 * @summary JLS 10.3 / 15.10.1 (Java SE 8): a negative dimension value causes a
 *          NegativeArraySizeException.
 * @run main ArrayCreationNegativeLength
 */

public class ArrayCreationNegativeLength {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Throwing { void run() throws Throwable; }
    static void checkThrows(String label, Throwing r) {
        try {
            r.run();
        } catch (NegativeArraySizeException expected) {
            check(true, label + " throws NegativeArraySizeException");
            return;
        } catch (Throwable x) {
            throw new AssertionError(label + ": expected NegativeArraySizeException, got " + x);
        }
        throw new AssertionError(label + ": expected NegativeArraySizeException");
    }

    public static void main(String[] args) {
        checkThrows("new int[-1]", () -> { Object o = new int[-1]; });
        checkThrows("new int[0][-1]", () -> { Object o = new int[0][-1]; });
        checkThrows("new String[-2]", () -> { Object o = new String[-2]; });

        int[] z = new int[0];
        check(z.length == 0, "zero length is legal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
