/*
 * @test
 * @summary JLS 3.8 (Java SE 8): an identifier is an unlimited-length sequence of
 *          Java letters and digits beginning with a Java letter, or the
 *          currency symbols $ and _.  In Java 8, `_` is itself a legal
 *          identifier (it became a keyword only in Java 9).
 * @run main Identifiers
 */

public class Identifiers {
    public static void main(String[] args) {
        int abc = 1;
        int $ = 2;
        int _ = 3;             // legal identifier in Java 8
        int $abc = 4;
        int _abc = 5;
        int a1 = 6;
        int α = 7;             // Unicode letter
        int MAX_VALUE = 8;

        if (abc + $ + _ + $abc + _abc + a1 + α + MAX_VALUE != 36)
            throw new AssertionError("identifiers");

        System.out.println("PASS");
    }
}
