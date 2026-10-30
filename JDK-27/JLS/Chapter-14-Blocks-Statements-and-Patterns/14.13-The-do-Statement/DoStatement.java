/*
 * @test
 * @summary JLS 14.13 / 14.13.1: a do statement executes its body at least once;
 *          a continue in the body evaluates the condition (possibly unboxed)
 *          before deciding whether to iterate again.
 * @run main DoStatement
 */

public class DoStatement {
    public static void main(String[] args) {
        int i = 0;
        do { i++; } while (false);
        if (i != 1) throw new AssertionError("do body not executed once: " + i);

        int n = 0;
        int count = 0;
        do {
            count++;
            if (count < 3) continue;   // continue evaluates the condition
            break;
        } while (n < 10);
        if (count != 3) throw new AssertionError("continue in do: " + count);

        System.out.println("PASS");
    }
}
