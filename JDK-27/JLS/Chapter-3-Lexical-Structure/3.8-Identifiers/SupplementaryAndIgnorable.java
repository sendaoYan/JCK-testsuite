/*
 * @test
 * @summary JLS 3.8: a supplementary character may be a Java letter;
 *          identifiers are equal after ignoring ignorable characters.
 * @run main SupplementaryAndIgnorable
 */
public class SupplementaryAndIgnorable {
    static int \ud835\udc82 = 20;
    public static void main(String[] args) {
        if (\ud835\udc82 != 20) {
            throw new AssertionError("supplementary identifier failed");
        }
        int \u0069\u0000 = 5;
        \u0069 = 6;
        if (\u0069 != 6) {
            throw new AssertionError("ignorable char made identifiers distinct");
        }
        System.out.println("PASS");
    }
}
