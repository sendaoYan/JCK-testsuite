/*
 * @test
 * @summary JLS 16.2.15 (Java SE 8): a variable assigned in the try block and in
 *          every catch block is definitely assigned after a try statement; a
 *          finally block also definitely assigns a variable it writes.
 * @run main TryCatchFinallyDA
 */

public class TryCatchFinallyDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x;
        try { x = 1; } catch (RuntimeException e) { x = 2; } finally { check(true, "finally runs"); }
        check(x == 1, "x assigned in try and catch");

        int y;
        try { throw new IllegalStateException(); }
        catch (IllegalStateException e) { y = 2; }
        finally { /* no assignment */ }
        check(y == 2, "y assigned in catch");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
