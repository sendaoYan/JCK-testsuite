/*
 * @test
 * @summary JLS 6.2 (Java SE 27): an identifier is a sequence of Java letters and
 *          Java digits; the first character must be a Java letter, "_", or "$",
 *          and later characters may also be digits.
 * @run main UnicodeIdentifier
 */

public class UnicodeIdentifier {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int a = 1;
        int α = 2;  // Greek letter alpha (a Java letter)
        int $ = 3;
        int _x = 4;
        int a1 = 5;
        int a_b = 6;

        check(a == 1, "ASCII letter");
        check(α == 2, "Unicode letter via escape");
        check($ == 3, "dollar sign start");
        check(_x == 4, "underscore start");
        check(a1 == 5, "digit after first character");
        check(a_b == 6, "underscore in the middle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
