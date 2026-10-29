/*
 * @test
 * @summary JLS 6.3.2.7: a labeled statement interacts with pattern variable
 *          scope through the "cannot complete normally" / reachable break rules.
 *          Here an if-then whose branch breaks the label introduces a pattern
 *          variable into the rest of the labeled block.
 * @run main LabeledStatement
 */

public class LabeledStatement {
    public static void main(String[] args) {
        Object o = "labelled";
        int len;
        L: {
            if (!(o instanceof String s)) {
                len = -1;
                break L;
            }
            len = s.length();
        }
        if (len != 8) throw new AssertionError("len " + len);

        System.out.println("PASS");
    }
}
