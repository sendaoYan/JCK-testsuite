/*
 * @test
 * @summary JLS 6.3.2.6: in a switch statement with switch rules, a pattern
 *          variable introduced by a switch label is matched in the associated
 *          switch rule block.
 * @run main SwitchStatement
 */

public class SwitchStatement {
    public static void main(String[] args) {
        Object o = "sw";
        switch (o) {
            case String s -> {
                if (!s.equals("sw")) throw new AssertionError("s");
            }
            default -> throw new AssertionError("unexpected default");
        }

        System.out.println("PASS");
    }
}
