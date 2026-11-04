/*
 * @test
 * @summary JLS 7.6 (Java SE 8): extra ";" tokens at the level of type
 *          declarations have no effect on the meaning of the compilation unit.
 * @run main StraySemicolons
 */

public class StraySemicolons {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(true, "stray semicolons tolerated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

;   // stray semicolon

class AlsoHere {}

;
