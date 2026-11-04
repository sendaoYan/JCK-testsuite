/*
 * @test
 * @summary JLS 14.7: a label's scope is its immediately contained statement, so a
 *          labeled break exits only that statement and the same label identifier
 *          may be reused in sibling statements.
 * @run main LabeledStatementScope
 */

public class LabeledStatementScope {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) {
        int r = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == 1) break;   // breaks only the inner loop
                r++;
            }
        }
        check(r == 3, "unlabeled break breaks innermost loop");

        int s = 0;
        outer:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == 1) break outer;   // breaks the labeled outer loop
                s++;
            }
        }
        check(s == 1, "labeled break breaks the outer loop");

        // The same label identifier may be reused in sibling blocks.
        {
            again: { break again; }
        }
        {
            again: { break again; }
        }
        check(true, "same label reused in sibling scopes");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
