/*
 * @test
 * @summary JLS 7.6: stray ";" tokens at the level of class/interface declarations
 *          have no effect on the meaning of the compilation unit.
 * @run main StraySemicolons
 */

public class StraySemicolons {
    public static void main(String[] args) {
        System.out.println("PASS");
    }
}
;
