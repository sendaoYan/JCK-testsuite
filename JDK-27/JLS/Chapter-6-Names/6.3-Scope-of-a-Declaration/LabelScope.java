/*
 * @test
 * @summary JLS 6.3 (Java SE 27): the scope of a label is the labeled statement;
 *          labels live in a namespace separate from variables, so a label and a
 *          local variable may share a simple name.
 * @run main LabelScope
 */

public class LabelScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int loop = 0;       // local variable named "loop"
        loop:              // label named "loop" (separate namespace)
        for (int i = 0; i < 2; i++) {
            loop++;        // refers to the variable
            if (i == 0) continue loop;   // refers to the label
        }
        check(loop == 2, "label and variable share a name without conflict");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
